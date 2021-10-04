package net.moewes.cloudui.quarkus.runtime.identity;

import java.util.Optional;

public class DefaultIdentity implements Identity {

    @Override
    public Optional<String> getBearer() {
        return Optional.empty();
    }
}
