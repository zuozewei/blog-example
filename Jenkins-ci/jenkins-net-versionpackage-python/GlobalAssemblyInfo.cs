/*
 * 全局程序集信息
 * GlobalAssemblyInfo.cs
 * 
 * 请把此文件引用到其他的项目中
*/
using System.Reflection;
using V3C.Client.Properties;

[assembly: AssemblyProduct("xxxx")]
[assembly: AssemblyCompany("xxxx科技股份有限公司")]
[assembly: AssemblyCopyright("Copyright(C) e-xxxx 2000-2020")]
[assembly: AssemblyVersion(RevisionClass.FullVersion)]
//产品版本号：统一使用2.0.0
[assembly: AssemblyInformationalVersionAttribute("2.0.1001")]
//程序中使用统一改为文件版本号
[assembly: AssemblyFileVersion(RevisionClass.FullVersion +
//只有Master、Release分支编译的版本才是正式版本，其他都是开发版。
#if !MASTER
     "-开发版"
#else
    ""
#endif
)]

namespace V3C.Client.Properties
{
    internal static class RevisionClass
    {
        public const string Major = "2";
        public const string Minor = "0";
        public const string Build = "1005";
        public const string Revision = "9151";
        public const string MainVersion = Major + "." + Minor;
        public const string FullVersion = Major + "." + Minor + "." + Build + "." + Revision;
    }

}

