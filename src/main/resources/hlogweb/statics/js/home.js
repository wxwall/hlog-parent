/**
 * Created by lenovo on 2016/9/12.
 */
var home = {};
home.init = function () {
    $(".sidebar a").each(function(){
        var self = this;

        $(self).click(function(event){
            event.stopPropagation();
            $(".sidebar a i").removeClass("text-green");
            $(".sidebar .active").removeClass("active");

            $(self).children("i").addClass("text-green");
            var $ul = $(self).parent().parent();
            $(self).parent().addClass("active");
            var dataurl = $(self).attr("data-url");
            if(typeof(dataurl) == 'undefined' || dataurl==null || dataurl.length == 0){
                breadcrumb = $(self).children("span").text();
                return;
            }
            if($(self).children("i").hasClass("fa-circle-o")){
                $(self).parents(".treeview:eq(0)").addClass("active");
                $(self).parents(".treeview:eq(0)").find("i:eq(0)").addClass("text-green");
                $("#breadcrumb").html(breadcrumb);
                $("#breadcrumb-child").html(" &gt;"+$(self).text());
            }else{
                $("#breadcrumb").html($(self).text());
                $("#breadcrumb-child").html("");
            }
            $.ajax({
                url: dataurl,
                type: "POST",
                dataType: "html",
                async: false,
                success: function(msg){
                    $("#main-content").html(msg);
                },
                complete:function () {

                },
                error:function () {
                    $("#main-content").html("加载出错，页面不存在！");
                }
            });
        });
    });
    $(".active a").click();
}

home.getRootPath = function () {
    var webroot=document.location.href;
    webroot=webroot.substring(webroot.indexOf('//')+2,webroot.length);
    webroot=webroot.substring(webroot.indexOf('/')+1,webroot.length);
    webroot=webroot.substring(0,webroot.indexOf('/'));
    return "/"+webroot;
}

$(function () {
    home.init();
})