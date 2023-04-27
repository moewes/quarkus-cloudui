package net.moewes.cloudui.quarkus.runtime.identity;



import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class IdentityProducer {

    @Produces
    @DefaultBean
    public Identity identity() {
        return new DefaultIdentity();
    }
}
