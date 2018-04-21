package io.scalecube.services;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;
import io.scalecube.services.api.ServiceMessage;

import org.reactivestreams.Publisher;

@Service
interface GreetingService {

  @ServiceMethod
  Publisher<String> greetingNoParams();

  @ServiceMethod
  Publisher<String> greeting(String string);

  @ServiceMethod
  Publisher<GreetingResponse> greetingRequestTimeout(GreetingRequest request);

  @ServiceMethod
  Publisher<GreetingResponse> greetingRequest(GreetingRequest string);

  @ServiceMethod
  Publisher<ServiceMessage> greetingMessage(ServiceMessage request);

  @ServiceMethod
  Publisher<Void> greetingVoid(GreetingRequest request);

}