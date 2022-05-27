package com.nixsolutions.internal;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@Operations(OpenSessionOperations.class)
@ConnectionProviders(OpenSessionConnectionProvider.class)
public class OpenSessionConfiguration {

}
