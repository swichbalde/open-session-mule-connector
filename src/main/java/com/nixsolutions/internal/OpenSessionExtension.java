package com.nixsolutions.internal;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;

@Xml(prefix = "opensession")
@Extension(name = "OpenSession")
@Configurations(OpenSessionConfiguration.class)
public class OpenSessionExtension {

}
