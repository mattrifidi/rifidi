/**
 * Created by Alejandro on 07/04/2015.
 */

app.service('MenuService', function($rootScope, $http, ServerService, CommonService){

    //Method that updates the list of menu servers
    this.updateMenuServers = function(){

        console.log('updateMenuServers');

        //Query the updated list of servers
        ServerService.callServerListService()
            .success(function (data, status, headers, config) {

                console.log('updateMenuServers.success response');

                //in rootScope attribute wa have the meno elements from servers root, so we need to compare
                //this new received list with servers inside menu
                var menuServers = $rootScope.elementList[0].children;

                console.log('menuServers: ');
                console.log(menuServers);

                //iterate over the received servers
                //if exists -> then update
                //if does not exist -> then add
                data.forEach(function (newServer) {

                    var newServerExists = false;

                    //iterate over the current list of servers in menu
                    menuServers.forEach(function (menuServer) {

                        if(!newServerExists && (newServer.displayName == menuServer.displayName) ){

                            newServerExists = true;

                            menuServer.restProtocol = newServer.restProtocol;
                            menuServer.ipAddress = newServer.ipAddress;
                            menuServer.restPort = newServer.restPort;
                            menuServer.host = newServer.restProtocol + '://' + newServer.ipAddress + ':' + newServer.restPort;

                            //then update the menuServer, with initial properties
                            //menuServer.iconClass = "server-disconnected";

                            //menuServer.status = 'CONNECTING';
                            //menuServer.tooltipText = 'Connecting';
                            //menuServer.allowSaveServerConfig = false;

                        }

                    });

                    if ( !newServerExists ){

                        //then add new server to menu list
                        newServer.collapsed = true;
                        newServer.elementName = newServer.displayName;
                        newServer.elementId = "server";
                        newServer.elementType = "server";
                        newServer.contextMenuId = "contextMenuServer";
                        newServer.children = [];
                        newServer.host = newServer.restProtocol + "://" + newServer.ipAddress + ":" + newServer.restPort;

                        changeServerStatusToConnecting(newServer);

                        console.log('going to add new server to menu');

                        menuServers.push(newServer);

                    }


                });

                //iterate over the menu servers
                //if exists -> then do nothing, because it was already updated in previous loop
                //if does not exist -> then delete from menu list of servers
                var serversToDelete = [];
                menuServers.forEach(function (menuServer) {

                    var menuServerExists = false;

                    //iterate over the new received server list
                    data.forEach(function (newServer) {

                        if( (newServer.displayName == menuServer.displayName) ){

                            menuServerExists = true;

                        }

                    });

                    if ( !menuServerExists ){

                        //then delete the server from menu (add to a list, and then when exit this menu servers loop, delete)
                        serversToDelete.push(angular.copy(menuServer));

                    }

                });

                if ( serversToDelete.length > 0 ){

                    //There is at least one server to delete, then delete

                    //iterate the list of menu servers, and delete
                    serversToDelete.forEach(function (serverToDelete) {

                        //iterate over the menu servers
                        var currentIndex = -1;
                        menuServers.forEach(function (menuServer) {

                            currentIndex++;
                            if( serverToDelete.displayName == menuServer.displayName ){

                                menuServers.splice(currentIndex,1);
                            }

                        });

                    });

                }

                //order the menu list of servers
                menuServers.sort( CommonService.compareElements );

                //call the method to update menu servers status
                updateMenuServersStatus();


            })
            . error(function (data, status, headers, config) {
                console.log("updateMenuServers.fail response");

            });


        //var serviceUrl = host + '/apps';
        //return $http({ method: 'GET', url: serviceUrl });

    };

    var changeServerStatusToConnecting = function(server){

        server.status = 'CONNECTING';
        server.tooltipText = 'Connecting';
        server.allowSaveServerConfig = false;
        server.iconClass = "server-disconnected";

    };

    var changeServerStatusToConnected = function(server) {

        server.status = 'CONNECTED';
        server.iconClass = "server-connected";
        server.allowSaveServerConfig = true;
        server.tooltipText = 'Connected';

    };


    //Method that updates the status of menu servers
    var updateMenuServersStatus = function(){

        console.log('updateMenuServersStatus');

        //get the reference to menu servers
        var menuServers = $rootScope.elementList[0].children;

        menuServers.forEach(function (menuServer) {

            var protocol = menuServer.restProtocol;
            var ipAddress = menuServer.ipAddress;
            var port = menuServer.restPort;

            //For each server make an asynchronous call to test whether the ping rest operation returns success
            ServerService.callPingServerService(protocol, ipAddress, port)
                .success(function (data, status, headers, config) {

                    console.log('updateMenuServersStatus.callPingServerService.success response');
                    console.log('updateMenuServersStatus.callPingServerService.headers:');
                    console.log('config:');
                    console.log(config);

                    var serverTimestamp = ServerService.getPingTimestampFromReceivedData(data);

                    //find the right server to change status to 'CONNECTED'

                    var originalUrl = config.url;
                    //extract the '/apps' suffix to get the host name
                    var pingResponseHost = getHostFromConfigPingResponse(config);
                    console.log('pingResponseHost:');
                    console.log(pingResponseHost);

                    if (serverTimestamp) {
                        console.log("server ping from host: " + pingResponseHost + ", timestamp: " + serverTimestamp);

                        //change server connecting status to connected

                        menuServers.forEach(function (serverToTest) {

                            console.log('inside menuservers loop');
                            console.log('serverToTest.host: ' + serverToTest.host);
                            console.log('pingResponseHost: ' + pingResponseHost);

                            if (serverToTest.host == pingResponseHost) {

                                console.log('going to change state to connected, on host:');
                                console.log(pingResponseHost);
                                //change server status to connected
                                changeServerStatusToConnected(serverToTest);
                            }
                        });

                    }


                })
                . error(function (data, status, headers, config) {

                    console.log('updateMenuServersStatus.callPingServerService error');
                    //change server status to 'CONNECTING'

                    var pingResponseHost = getHostFromConfigPingResponse(config);
                    console.log('pingErrorResponse.pingResponseHost:');
                    console.log(pingResponseHost);

                    //change server to connecting status

                    menuServers.forEach(function (server) {

                        if (server.host == pingResponseHost) {

                            console.log('going to change state to connecting, on host:');
                            console.log(pingResponseHost);
                            //change server status
                            changeServerStatusToConnecting(server);
                        }
                    });
                });
        });
    };

    //Gets the host protocol://ipaddress:port from config returned by ping rest operation
    //it assumes the rest operation is like protocol://ipaddress:port/ping
    var getHostFromConfigPingResponse = function(config){

        return config.url.substring(0, config.url.lastIndexOf("/"));

    };


    //Method that calls the API to stop application
    this.callStopAppService = function(host, appId){

        console.log('callStopAppService');
        console.log('appId');
        console.log(appId);
        var serviceUrl = host + '/stopapp/' + appId;
        return $http({ method: 'GET', url: serviceUrl });

    };

    //Method that calls the API to start application
    this.callStartAppService = function(host, appId){

        console.log('callStartAppService');
        console.log('appId');
        console.log(appId);
        var serviceUrl = host + '/startapp/' + appId;
        return $http({ method: 'GET', url: serviceUrl });

    };

    // Method that takes the xml response from server and returns the applications
    // belonging to specific group

    this.getAppsFromReceivedData = function(data, groupName){

        var xmlData = CommonService.getXmlObjectFromXmlServerData(data);

        //get the xml response and extract the values to construct the app list where group matches
        var appsXmlVector = xmlData.getElementsByTagName("app");

        var appsToReturn = [];

        for (var index = 0; index < appsXmlVector.length; index++) {

            var id = appsXmlVector[index].getElementsByTagName("id")[0].childNodes[0].nodeValue;
            var number = appsXmlVector[index].getElementsByTagName("number")[0].childNodes[0].nodeValue;
            var status = appsXmlVector[index].getElementsByTagName("status")[0].childNodes[0].nodeValue;

            //As id comes in the form 'AppGroup:AppName' we need to split it into two different variables

            var localGroupName = id.split(":")[0];
            var appName = id.split(":")[1];

            if (localGroupName == groupName) {

                //Add the application to app list
                var appElement = {
                    "number": number,
                    "status": status,
                    "groupName": groupName,
                    "appName": appName
                };

                appsToReturn.push(appElement);

            }
        }

        return appsToReturn;

    };

    // Method that extracts from url used to stop app, the app id parameter
    // It is expected the url has the form: "http://localhost:8111/stopapp/{appNumber}"

    this.extractAppIdFromStopAppUrl = function(url){

        //console.log('extractAppIdFromStopAppUrl');
        //console.log('extractAppIdFromStopAppUrl.url:');
        //console.log(url);

        //find the text 'stopapp/' and extract value after that string
        var word = 'stopapp/';
        var indexInicio = url.lastIndexOf(word) + word.length;
        var appId = url.substring(indexInicio, url.length);

        //console.log("extractAppIdFromStopAppUrl.appId:");
        //console.log(appId);

        return appId;

    }


});
