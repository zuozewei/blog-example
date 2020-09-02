
if(Branch.equals("dev")){
    lineList = new File("D:\\Jenkins-workspace\\Jenkins\\workspace\\xxxx-WEBAPP-Dev\\package_history.txt").readLines()
    lineList.sort{it}
    Collections.reverse(lineList)
    lineList.each {
        return it;
    }
}else if (Branch.equals("release")){
    lineList = new File("D:\\Jenkins-workspace\\Jenkins\\workspace\\xxxx-WEBAPP-Release\\package_history.txt").readLines()
    lineList.sort{it}
    Collections.reverse(lineList)
    lineList.each {
        return it;
    }
}else {
    return ["Unknown Hosts"]
}
