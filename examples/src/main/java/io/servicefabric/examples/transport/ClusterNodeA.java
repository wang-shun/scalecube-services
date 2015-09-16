package io.servicefabric.examples.transport;

import io.servicefabric.cluster.Cluster;
import io.servicefabric.cluster.ClusterMessage;
import io.servicefabric.cluster.ICluster;
import io.servicefabric.examples.Greetings;
import io.servicefabric.transport.TransportMessage;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Basic example for member transport between cluster members to run the example Start ClusterNodeA and cluster
 * ClusterNodeB A listen on transport messages B send message to member A.
 * 
 * @author ronen hamias
 *
 */
public class ClusterNodeA {


  public static final Func1<ClusterMessage, Boolean> GREETINS_PREDICATE = new Func1<ClusterMessage, Boolean>() {
    @Override
    public Boolean call(ClusterMessage t1) {
      return t1.message().data() != null && Greetings.class.equals(t1.message().data().getClass());
    }
  };

  public static void main(String[] args) {
    // start cluster node that listen on port 3000
    ICluster clusterA = Cluster.newInstance(3000).join();

    // Filter and subscribe to greetings messages:
    clusterA.listen().filter(GREETINS_PREDICATE).subscribe(new Action1<ClusterMessage>() {
      @Override
      public void call(ClusterMessage t1) {
        System.out.println(t1.message().data());
      }
    });
  }

}