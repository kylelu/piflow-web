var isTableLoading = true;

function initProcessGroupDatatablePage(testTableId, url, searchInputId) {
    var table = "";
    layui.use('table', function () {
        table = layui.table;

        //Method-level rendering
        table.render({
            elem: '#' + testTableId
            , url: url
            , cols: [[
                {
                    field: 'appId', title: 'ProcessGroupId', sort: true, templet: function (data) {
                        return ('<div name="processAppId">' + data.appId + '</div>');
                    }
                },
                {field: 'name', title: 'Name', sort: true},
                {field: 'description', title: 'Description', sort: true},
                {
                    field: 'startTime', title: 'StartTime', sort: true, templet: function (data) {
                        data.startTime = data.startTime ? data.startTime : "";
                        return ('<div id="' + data.id + 'startTime" name="processStartTime" >' + data.startTime + '</div>');
                    }
                },
                {
                    field: 'endTime', title: 'EndTime', sort: true, templet: function (data) {
                        data.endTime = data.endTime ? data.endTime : "";
                        return ('<div id="' + data.id + 'endTime" name="processEndTime">' + data.endTime + '</div>');
                    }
                },
                {
                    field: 'description', title: 'Progress', sort: true, templet: function (data) {
                        var progressHtmlStr = '<div>' +
                            '<p id="' + data.id + 'Info">progress:' +
                            (data.progress ? (data.progress + '%') : '0.00%') +
                            '</p>' +
                            '<progress id="' + data.id + '" max="100" value="' +
                            (data.progress ? (data.progress) : '0.00')
                            + '">' +
                            '</progress>' +
                            '</div>';
                        return progressHtmlStr;
                    }
                },
                {
                    field: 'state', title: 'Status', sort: true, templet: function (data) {
                        return (data.state ? data.state.text : '');
                    }
                },
                {
                    field: 'right', title: 'Actions', sort: true, height: 100, templet: function (data) {
                        return responseActionHandler(data);
                    }
                }
            ]]
            , id: testTableId
            , page: true
        });
    });

    $("#" + searchInputId).bind('input propertychange', function () {
        searchMonitor(table, testTableId, searchInputId);
    });
}

//Results returned in the background
function responseActionHandler(res) {
    if (res) {
        var actionsHtmlStr = '<p style="width: 100%; text-align: center" >' +
            '<a class="btn" ' +
            'href="javascript:void(0);" ' +
            'onclick="javascript:openProcessGroup(\'' + res.id + '\');" ' +
            'style="margin-right: 2px;">' +
            '<i class="icon-share-alt icon-white"></i>' +
            '</a>' +
            '<a class="btn" ' +
            'href="javascript:void(0);" ' +
            'onclick="javascript:selectRunMode(\'' + res.id + '\',\'' + res.parentProcessId + '\',\'null\');" ' +
            'style="margin-right: 2px;">' +
            '<i class="icon-play icon-white"></i>' +
            '</a>' +
            '<a class="btn" ' +
            'href="javascript:void(0);" ' +
            'onclick="javascript:listStopProcessGroup(\'' + res.id + '\');" ' +
            'style="margin-right: 2px;">' +
            '<i class="icon-stop icon-white"></i>' +
            '</a>' +
            '<a class="btn" ' +
            'href="javascript:void(0);" ' +
            'onclick="javascript:delProcessGroup(\'' + res.id + '\');" ' +
            'style="margin-right: 2px;">' +
            '<i class="icon-trash icon-white"></i>' +
            '</a>' +
            '</p>';
        return actionsHtmlStr;
    }
    return "";
}

function searchMonitor(layui_table, layui_table_id, searchInputId) {
    //Perform overload
    layui_table.reload(layui_table_id, {
        page: {
            curr: 1 //Start again on page 1
        }
        , where: {param: $('#' + searchInputId).val()}
    }, 'data');
}

function processGroupListMonitoring() {
    console.log("--------");
    var arrayObj = new Array();
    var processAppIds = $("div[name='processAppId']");
    var processStartTimes = $("div[name='processStartTime']");
    var processStates = $("div[name='processState']");
    if (processAppIds && processStartTimes && processStates) {
        if (processAppIds.length == processStartTimes.length && processStartTimes.length === processStates.length) {
            for (var i = 0; i < processAppIds.length; i++) {
                if (processAppIds[i].innerHTML != "") {
                    if (processStates[i].innerHTML != "No state" && processStates[i].innerHTML == "STARTED") {
                        arrayObj.push(processAppIds[i].innerHTML);
                    }
                }
                if (processStartTimes[i].innerHTML == '') {
                    arrayObj.push(processAppIds[i].innerHTML);
                }
            }
            if (processAppIds.length > 0) {
                isTableLoading = false;
            }
        }
    } else {
        isTableLoading = true;
    }
    if (isTableLoading) {
        return;
    } else {
        if (arrayObj.length == 0) {
            window.clearInterval(timer);
            return;
        }
        $.ajax({
            cache: true,
            type: "get",
            url: "/piflow-web/processGroup/getAppInfoList",
            data: {arrayObj: arrayObj},
            async: true,
            traditional: true,
            error: function (request) {
                console.log("error");
                return;
            },
            success: function (data) {
                if (null != data) {
                    var dataMap = JSON.parse(data);
                    if (200 === dataMap.code) {
                        if (arrayObj && arrayObj.length > 0) {
                            for (var i in arrayObj) {
                                var strAppID = arrayObj[i];
                                if (strAppID && '' !== strAppID) {
                                    var processGroup = dataMap[strAppID];
                                    if (processGroup && '' !== processGroup) {
                                        if (processGroup.id && '' != processGroup.id) {
                                            document.getElementById("" + processGroup.id + "").value = processGroup.progress;
                                            document.getElementById("" + processGroup.id + "Info").innerHTML = "progress:" + processGroup.progress + "%";
                                            if (processGroup.state && "" !== processGroup.state) {
                                                document.getElementById("" + processGroup.id + "state").innerHTML = processGroup.state.text;
                                            }
                                            if (processGroup.startTime && "" !== processGroup.startTime) {
                                                document.getElementById("" + processGroup.id + "startTime").innerHTML = processGroup.startTime;
                                            }
                                            if (processGroup.endTime && "" !== processGroup.endTime) {
                                                document.getElementById("" + processGroup.id + "endTime").innerHTML = processGroup.endTime;
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        });
    }
}

//Select run mode
function selectRunMode(id, processId, parentProcessId, runMode) {
    var runModeContent = '<div style="width: 100%;">'
        + '<div style="width: 210px;height: 50px;line-height: 50px;overflow: hidden;text-align: center;">'
        + '<button type="button" class="btn btn-default" onclick="listRunProcessGroup(\'' + id + '\',null)">Run</button>&nbsp;'
        // + '<button type="button" class="btn btn-default" onclick="listRunProcessGroup(\'' + id + '\',\'DEBUG\')">Debug</button>&nbsp;'
        + '<button type="button" class="btn btn-default" onclick="cancelListRunProcessGroup()">Cancel</button>'
        + '</div>'
        + '</div>';
    layer.open({
        type: 1,
        title: '<span style="color: #269252;">Select Run Mode</span>',
        shadeClose: true,
        closeBtn: 1,
        shift: 7,
        //area: ['600px', '200px'], //Width height
        skin: 'layui-layer-rim', //Add borders
        content: runModeContent
    });
}

function cancelListRunProcessGroup() {
    layer.closeAll();
    $('#fullScreen').hide();
    return;
}

//run
function listRunProcessGroup(id, runMode) {
    $('#fullScreen').show();
    var data = {
        id: id,
    }
    if (runMode) {
        data.runMode = runMode;
    }
    $.ajax({
        cache: true,//Keep cached data
        type: "POST",//Request type post
        url: "/piflow-web/processGroup/runProcessGroup",//This is the name of the file where I receive data in the background.
        //data:$('#loginForm').serialize(),//Serialize the form
        data: data,
        async: true,//Setting it to true indicates that other code can still be executed after the request has started. If this option is set to false, it means that all requests are no longer asynchronous, which also causes the browser to be locked.
        error: function (request) {//Operation after request failure
            alert("Request Failed");
            $('#fullScreen').hide();
            return;
        },
        success: function (data) {//Operation after request successful
            //console.log("success");
            var dataMap = JSON.parse(data);
            if (200 === dataMap.code) {
                //alert(dataMap.errorMsg);
                window.location.reload();
                var windowOpen = window.open("/piflow-web/processGroup/getProcessGroupById?processGroupId=" + dataMap.processGroupId);
                if (windowOpen == null || typeof (windowOpen) == 'undefined') {
                    alert('The window cannot be opened. Please check your browser settings.')
                }
            } else {
                alert("Startup Failed");
                $('#fullScreen').hide();
            }

        }
    });
}

//stop
function listStopProcessGroup(processGroupID) {
    $('#fullScreen').show();
    $.ajax({
        cache: true,//Keep cached data
        type: "POST",//Request type post
        url: "/piflow-web/processGroup/stopProcessGroup",//This is the name of the file where I receive data in the background.
        //data:$('#loginForm').serialize(),//Serialize the form
        data: {
            processGroupId: processGroupID
        },
        async: true,//Setting it to true indicates that other code can still be executed after the request has started. If this option is set to false, it means that all requests are no longer asynchronous, which also causes the browser to be locked.
        error: function (request) {//Operation after request failure
            alert("Request Failed");
            $('#fullScreen').hide();
            return;
        },
        success: function (data) {//Operation after request successful
            //console.log("success");
            var dataMap = JSON.parse(data);
            if (200 === dataMap.code) {
                alert(dataMap.errorMsg);
                window.location.reload();
            } else {
                alert("Stop Failed:" + dataMap.errorMsg);
            }
            $('#fullScreen').hide();

        }
    });
}

//remove
function delProcessGroup(processGroupID) {
    $('#fullScreen').show();
    $.ajax({
        cache: true,//Keep cached data
        type: "get",//get
        url: "/piflow-web/processGroup/delProcessGroup",//This is the name of the file where I receive data in the background.
        //data:$('#loginForm').serialize(),//Serialize the form
        data: {
            processGroupId: processGroupID
        },
        async: true,//Setting it to true indicates that other code can still be executed after the request has started. If this option is set to false, it means that all requests are no longer asynchronous, which also causes the browser to be locked.
        error: function (request) {//Operation after request failure
            alert("Request Failed");
            $('#fullScreen').hide();
            return;
        },
        success: function (data) {//Operation after request successful
            var dataMap = JSON.parse(data);
            if (200 === dataMap.code) {
                alert(dataMap.errorMsg);
                window.location.reload();
            } else {
                alert("Delete Failed" + dataMap.errorMsg);
            }
            $('#fullScreen').hide();
        }
    });
}

function openProcessGroup(processGroupId) {
    var windowOpen = window.open('/piflow-web/processGroup/getProcessGroupById?processGroupId=' + processGroupId);
    //var windowOpen = window.open('/piflow-web/mxGraph/drawingBoard?drawingBoardType=PROCESS&load=' + processGroupId);
    if (windowOpen == null || typeof (windowOpen) == 'undefined') {
        alert('The window cannot be opened. Please check your browser settings.')
    }
}
