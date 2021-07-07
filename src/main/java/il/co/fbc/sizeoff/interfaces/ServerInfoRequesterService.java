package il.co.fbc.sizeoff.interfaces;

import il.co.fbc.sizeoff.services.bo.ServerInfoBo;

public interface ServerInfoRequesterService {
    ServerInfoBo get (String serverName);
}
