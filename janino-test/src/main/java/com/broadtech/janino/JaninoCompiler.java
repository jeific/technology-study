package com.broadtech.janino;

import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IScriptEvaluator;

/**
 * create by 2017/12/29 15:24<br>
 *
 * @author Yuanjun Chen
 */
public class JaninoCompiler {

    /**
     * @param code        代码
     * @param argVarNames {@link IPlugin#exe(String)} 输入参数名，需要和code里面使用的输入参数名保持一致
     * @return code编译产生的 {@link IPlugin}实例
     */
    public IPlugin compile(String code, String[] argVarNames, String[] importLists) throws Exception {
        IScriptEvaluator se1 = CompilerFactoryFactory.getDefaultCompilerFactory().newScriptEvaluator();
        if (importLists != null && importLists.length > 0)
            se1.setDefaultImports(importLists);
        //se1.setExtendedClass(BuiltinFunctions.class); // 设置父类
        return (IPlugin) se1.createFastEvaluator(code, IPlugin.class, argVarNames);
    }
}
