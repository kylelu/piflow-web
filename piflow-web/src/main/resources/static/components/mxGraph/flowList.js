var flowTable;

function initDatatableFlowPage(testTableId, url, searchInputId) {
    var table = "";
    layui.use('table', function () {
        table = layui.table;

        //Method-level rendering
        table.render({
            elem: '#' + testTableId
            , url: url
            , cols: [[
                {field: 'name', title: 'Name', sort: true},
                {field: 'description', title: 'Description', sort: true},
                {field: 'crtDttm', title: 'CreateTime', sort: true},
                {
                    field: 'right', title: 'Actions', sort: true, height: 100, templet: function (data) {
                        return responseHandlerFlow(data);
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
function responseHandlerFlow(res) {
    if (res) {
        var actionsHtmlStr = '<div style="width: 100%; text-align: center" >'
            + '<input type="button" class="btn-block" onclick="importFlow(\'' + res.id + '\')" value="Import Flow"/>'
            + '</div>';
        return actionsHtmlStr;
    }
    return '';
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

function importFlow(flowId) {
    $.ajax({
        cache: true,
        type: "POST",
        url: "/piflow-web/flowGroup/copyFlowToGroup",
        data: {"flowId": flowId, "flowGroupId": loadId},
        async: true,
        error: function (request) {
            //alert("Jquery Ajax request error!!!");
            return;
        },
        success: function (data) {
            var dataMap = JSON.parse(data);
            if (200 === dataMap.code) {
                loadXml(dataMap.xmlStr);
                alert("cheng_gong cheng_gong cheng_gong cheng_gong cheng_gong cheng_gong cheng_gong cheng_gong");
            } else {
                alert(dataMap.errorMsg);
            }
        }
    });
}