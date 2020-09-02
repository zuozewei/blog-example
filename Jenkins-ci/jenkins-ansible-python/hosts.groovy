dev = ["Dev_ALL","Dev_AutoTest","Dev_FunctionTest","Dev_Develop"]
release = ["Release_ALL","Release_AutoTest","Release_FunctionTest","Release_Develop"]

if(Branch.equals("dev")){
    return dev
}else if (Branch.equals("release")){
    return release
}else {
    return ["Unknown Hosts"]
}