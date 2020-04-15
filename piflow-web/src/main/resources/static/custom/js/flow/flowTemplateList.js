function initDatatableFlowTemplatePage(testTableId, url) {
    layui.use('table', function () {
        var table = layui.table;

        //Method-level rendering
        table.render({
            elem: '#' + testTableId
            , url: url
            , cols: [[
                {field: 'name', title: 'TemplateName', sort: true}
                , {field: 'crtDttm', title: 'CreateTime', sort: true}
                , {
                    field: 'right', title: 'Actions', sort: true, height: 100, templet: function (data) {
                        return responseHandlerTemplate(data);
                    }
                }
            ]]
            , id: testTableId
            , page: true
        });

        var $ = layui.$, active = {
            reload: function () {
                //var demoReload = $('#demoReload');

                //Perform overload
                table.reload(testTableId, {
                    page: {
                        curr: 1 //Start again on page 1
                    }
                    , where: {param: $('#demoReload').val()}
                }, 'data');
            }
        };

        $('.demoTable .layui-btn').on('click', function () {
            var type = $(this).data('type');
            active[type] ? active[type].call(this) : '';
        });
    });
}

//Results returned in the background
function responseHandlerTemplate(res) {
    var actionsHtmlStr = '<div style="width: 100%; text-align: center" >' +
        '<a class="btn" ' +
        'href="javascript:void(0);" ' +
        'onclick="javascript:downloadFlowGroupTemplate(\'' + res.id + '\');" ' +
        'title="download template">' +
        '<i class="icon-download icon-white"></i>' +
        '</a>' +
        '<a class="btn" href="javascript:void(0);" ' +
        'onclick="javascript:deleteFlowGroupTemPlate(\'' + res.id + '\',\'' + res.name + '\'); "' +
        'title="delete template" > ' +
        '<i class="icon-trash icon-white"></i>' +
        '</a>' +
        '</div>';
    return actionsHtmlStr;
}

function searchTemplatePage() {
    flowGroupTemplateTable.ajax.reload();
}

function deleteFlowGroupTemPlate(id, name) {
    layer.confirm("Are you sure to delete '" + name + "' ?", {
        btn: ['confirm', 'cancel'] //button
        , title: 'Confirmation prompt'
    }, function () {
        $.ajax({
            cache: true,//Keep cached data
            type: "get",//Request type post
            url: "/piflow-web/flowGroupTemplate/deleteFlowGroupTemplate",//This is the name of the file where I receive data in the background.
            data: {id: id},
            async: true,//Setting it to true indicates that other code can still be executed after the request has started. If this option is set to false, it means that all requests are no longer asynchronous, which also causes the browser to be locked.
            error: function (request) {//Operation after request failure
                return;
            },
            success: function (data) {//Operation after request successful
                if (data > 0) {
                    layer.msg('Delete Success', {icon: 1, shade: 0, time: 2000}, function () {
                        location.reload();
                    });
                } else {
                    layer.msg('Delete failed', {icon: 2, shade: 0, time: 2000}, function () {
                    });
                }
            }
        });
    }, function () {
    });
}

function initAll(url) {
    window.location.href = url;
}

function listUploadFlowGroupTemplateBtn() {
    document.getElementById("flowGroupTemplateFile").click();
}

function listUploadFlowGroupTemplate() {
    if (!listFileTypeCheck()) {
        return false;
    }
    var formData = new FormData($('#uploadForm')[0]);
    $.ajax({
        type: 'post',
        url: "/piflow-web/flowGroupTemplate/uploadXmlFile",
        data: formData,
        cache: false,
        processData: false,
        contentType: false,
    }).success(function (data) {
        var dataMap = JSON.parse(data);
        if (200 === dataMap.code) {
            layer.msg(dataMap.errorMsg, {icon: 1, shade: 0, time: 2000}, function () {
                location.reload();
            });
        } else {
            layer.msg(dataMap.errorMsg, {icon: 2, shade: 0, time: 2000}, function () {
            });
        }
    }).error(function () {
        layer.msg("Upload failure", {icon: 2, shade: 0, time: 2000}, function () {
        });
    });
}

function listFileTypeCheck() {
    var obj = document.getElementById('flowGroupTemplateFile');
    if (obj.value == null || obj.value == '') {
        layer.msg('please upload the XML file', {icon: 2, shade: 0, time: 2000}, function () {
        });
        this.focus()
        return false;
    }
    var length = obj.value.length;
    var charindex = obj.value.lastIndexOf(".");
    var ExtentName = obj.value.substring(charindex, charindex + 4);
    if (!(ExtentName == ".xml")) {
        layer.msg('please upload the XML file', {icon: 2, shade: 0, time: 2000}, function () {
        });
        this.focus()
        return false;
    }
    return true;
}

function downloadFlowGroupTemplate(id) {
    window.location.href = "/piflow-web/flowGroupTemplate/templateDownload?flowGroupTemplateId=" + id;
}

