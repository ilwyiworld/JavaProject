package com.yiworld

fun sum(a:Int,b:Int):Int{
    return a+b;
}

fun vars(vararg v:Int){
    for(vt in v){
        print(vt)
    }
}

// 测试
fun main(args: Array<String>) {
    //vars(1,2,3,4,5)  // 输出12345

    var a = 1
    // 模板中的简单名称：
    val s1 = "a is $a"
    println(s1)
    a = 2
    // 模板中的任意表达式：
    val s2 = "${s1.replace("is", "was")}, but now is $a"
    println(s2)

    //类型后面加?表示可为空
    var age: String? = "23"
    //抛出空指针异常
    val ages = age!!.toInt()
    //不做处理返回 null
    val ages1 = age?.toInt()
    //age为空返回-1
    val ages2 = age?.toInt() ?: -1
}