package il.co.fbc.sizeoff.interfaces;

import il.co.fbc.sizeoff.services.bo.ServerInfoBo;

import java.util.function.Consumer;

public interface UpdateServerInfoService {
    void update(final String server, Consumer<ServerInfoBo> onSuccess, Consumer<Throwable> onError);
}
