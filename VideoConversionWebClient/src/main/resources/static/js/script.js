$(document).ready(function () {
    var movielist = "";
    getfiles();

    console.log(movielist);
    /*$("#moviefiles").on('click', '.delfile', function(){
        var idmovie = $(this).data("f");
        removefile(movielist[idmovie]);
    });*/

    $("#files").on('click', '.addconversion', function () {
        var idmovie = $(this).data("f");
        var myrequest = new Object();
        myrequest.path = movielist[idmovie];
        myrequest.format = "none";
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("POST", "https://35.224.228.254:42308/convert", true);
        xmlhttp.setRequestHeader("Content-Type", "application/json");
        xmlhttp.send(JSON.stringify(myrequest));
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                newconversion = JSON.parse(xmlhttp.responseText);
                console.log(newconversion.uuid);
                startWebSocket(newconversion.uuid);
                $('#list').append("<div data-conv="+newconversion.uuid+" class=\"alert bg-secondary\" role=\"alert\">\n" +
                    "                <h4 class=\"alert-heading\">"+myrequest.path+"</h4>\n" +
                    "                <p>Conversion en cours</p>\n" +
                    "                <hr>\n" +
                    "                <p class=\"mb-0\"><div class=\"progress\">\n" +
                    "                <div class=\"progress-bar progress-bar-striped progress-bar-animated\" role=\"progressbar\" aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width: 0%\"></div>\n" +
                    "            </div></p>\n" +
                    "            </div>");
            }
        }
    });

    function startWebSocket(id){
        var socket = new WebSocket('wss://35.224.228.254:42308/conversion_status');
        var id2 = id;
        socket.onopen = function () {
            console.log('Connected!');
            socket.send(id2);
        };
        socket.onmessage = function (event) {
            var obj = [{uuid:id,progression:parseFloat(event.data)}];
            console.log(obj);
            if(event.data == "100.0")
            {
                displayrunning(obj);
                socket.close()
            }else {
                displayrunning(obj);
                socket.send(id2);
            }
        };
        socket.onclose = function () {
            console.log('Lost connection!');
        };
        socket.onerror = function () {
            console.log('Error!');
        };
    }




    function getfiles() {
        var movies = [];
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("GET", "https://35.224.228.254:42308/directories", false);
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200){
                movies = JSON.parse(xmlhttp.responseText);
                displaymovies(movies);
            }
        };
        xmlhttp.send(null);
    }

    function displaymovies(data) {
        $( "#files" ).empty();
        movielist = data;
        $.each(data, function(i, obj) {
            $( "#files" ).append("<li class=\"media rounded  ml-2 mr-2 mb-5 pr-4 shadow\">\n" +
                "                    <div class=\"media-body\">\n" +
                "                        <div class=\"row\">\n" +
                "                            <div class=\"col-8\">\n" +
                "                                <h5 class=\"mt-0 mb-1\">"+obj+"</h5>\n"+
                "                            </div>\n" +
                "                            <div class=\"col-4 align-self-center\">\n" +
                "                                <div class=\"btn-group \" role=\"group\">\n" +
                "                                    <button data-f="+i+" type=\"button\" class=\"addconversion btn btn-outline\">Convertir</button>\n" +
                "                                </div>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </li>");
        });
    }

    function displayrunning(data){

        $.each(data, function (i, obj) {
            uuid = obj.uuid;
            $('div[data-conv="' + uuid + '"]').find(".progress-bar").attr("aria-valuenow",obj.progression);
            $('div[data-conv="' + uuid + '"]').find(".progress-bar").width(obj.progression+"%");
            if(obj.progression == "100" && $('[data-conv="' + uuid + '"]').data("conv") == uuid ){
               $('[data-conv="' + uuid + '"]').remove();
            }
        })
    }

});

