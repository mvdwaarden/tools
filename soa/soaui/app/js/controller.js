var app = angular.module('app');

app
        .controller('app', ['$scope', '$mdSidenav', function ($scope, $sidenav) {
                $scope.menu_open = function () {
                    $sidenav('left').open();
                };
                $scope.menu_close = function () {
                    $sidenav('left').close();
                }
            }])
        .controller('start', ['$scope', function ($scope) {
                $scope.do_draw = function () {                    
                    d3.json('http://localhost:8080/soatoolsrv/file/service.status.json',d3_do_draw);                    
                };
            }])
        .controller('soaui', ['$scope', '$interval', 'soatoolsrv', function ($scope, $interval, srv) {
                $scope.watch = false;
                $scope.handles = new Array();
                $scope.func = 'orcl';
                $scope.tab = {selectedIndex: 0};
                $scope.get_status = function () {
                    srv.status($scope.handle, function (response) {
                        $scope.status = response;
                        response.data.log.reverse();
                    });
                };
                $scope.get_list = function () {
                    srv.list(function (response) {
                        $scope.config = response.data.list[0];
                        $scope.configs = response.data.list;
                    });
                };
                $scope.do_function = function () {
                    srv.soatool($scope.func, $scope.config, function (response) {
                        $scope.handle = response.data.handle;
                        nfohandle = new Object();
                        nfohandle.handle = response.data.handle;
                        nfohandle.config = $scope.config;
                        $scope.handles.unshift(nfohandle);
                    });
                };
                $scope.do_watch = function () {
                    if ($scope.watch && angular.isUndefined($scope.watch_promis)) {
                        $scope.watch_promis = $interval(function () {
                            $scope.get_status();
                        }, 500);
                    } else if (!$scope.watch && angular.isDefined($scope.watch_promis)) {
                        $interval.cancel($scope.watch_promis);
                        $scope.watch_promis = undefined;
                    }
                };
                $scope.do_query = function() {
                    srv.query($scope.query,$scope.config,function(response){
                       d3_do_clear_svg();
                       $scope.result_urls = response.data.list;  
                       d3_do_draw_svg($scope.result_urls[0]);
                    });
                };
                 $scope.do_queryInit = function() {
                    srv.queryInit($scope.query,$scope.config,function(response){
                        nfohandle = new Object();
                        nfohandle.handle = response.data.handle;
                        nfohandle.config = $scope.config;
                        $scope.handles.unshift(nfohandle);
                    });
                };
                 $scope.get_queryList = function() {
                    srv.listQueries($scope.query,$scope.config,function(response){
                       $scope.query = response.data.list[0];
                        $scope.queries = response.data.list;                   
                    });
                };
                $scope.do_upload = function () {
                    srv.upload($scope.file, function (response) {
                        $scope.status = response;
                        if (angular.isDefined(response.data.log)) {
                            response.data.log.reverse();
                        }
                    });
                };
                $scope.get_list();
                $scope.get_queryList();
            }
        ]);
