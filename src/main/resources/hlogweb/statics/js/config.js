//# sourceURL=config.js

var config = {};
var cfgEditor = null;
config.highlight = function () {
    cfgEditor = CodeMirror.fromTextArea(document.getElementById("txt-config"), {
        lineNumbers: true
    });
}

config.init = function () {
    config.highlight();
    config.loadFileList();
}


config.loadFileList = function () {
    $.ajax({
        url: home.getRootPath()+"/hlogweb/action/files",
        type: "POST",
        success: function(msg){
            if (msg == "-1") {
                alert("请求不支持");
                return;
            }
            var files = msg.split(",");
            $("#text-select").html("");
            for (var i = 0; i <files.length;i++){
                if(files[i].length>1){
                    $("#text-select").append('<option value="'+files[i]+'">'+files[i]+'</option>');
                }
            }
            config.loadFile(files[0]);
        },
        complete:function () {

        },
        error:function () {
            alert("获取配置文件列表失败");
        }
    });
}

config.loadFile = function (fileName) {
    $.ajax({
        url: home.getRootPath()+"/hlogweb/action/config?file="+fileName,
        type: "POST",
        success: function(msg){
            if (msg == "-1") {
                alert("请求不支持");
                return;
            }
            cfgEditor.doc.setValue(msg);
        },
        complete:function () {

        },
        error:function () {
            alert("获取配置文件失败");
        }
    });
}

config.change =function () {
    config.loadFile($("#text-select").val());
}

config.save = function () {
    $.ajax({
        url: home.getRootPath()+"/hlogweb/action/save?file="+$("#text-select").val(),
        type: "POST",
        data:cfgEditor.doc.getValue(),
        success: function(msg){
            if (msg == "-1") {
                alert("请求不支持");
                return;
            }
            alert(msg);
        },
        complete:function () {

        },
        error:function () {
            alert("保存配置文件失败");
        }
    });
}



$(function () {
    config.init();
})

