package net.moewes.cloudui.quarkus.runtime.identity;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import io.quarkus.arc.DefaultBean;

@Dependent
public class IdentityProducer {

    @Produces
    @DefaultBean
    public Identity identity() {
        return new DefaultIdentity();
    }
}
