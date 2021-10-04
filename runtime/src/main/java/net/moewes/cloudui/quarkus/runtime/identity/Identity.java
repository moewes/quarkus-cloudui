package net.moewes.cloudui.quarkus.runtime.identity;

import java.util.Optional;

public interface Identity {

    Optional<String> getBearer();
}
