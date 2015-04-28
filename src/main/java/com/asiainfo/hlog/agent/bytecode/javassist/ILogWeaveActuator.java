package com.asiainfo.hlog.agent.bytecode.javassist;

import java.util.List;

/**
 * Created by c on 2015/3/17.
 */
public interface ILogWeaveActuator {

    LogWeaveCode executeWeave(LogWeaveContext logWeaveContext, List<ILogWeave> logWeaves);
}
