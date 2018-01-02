var app = angular.module('app', ['ngMaterial','ngRoute'])
        .config(['$mdThemingProvider', function ($theme) {
                //$theme.theme('default').dark();
            }])
        .config(['$routeProvider', function ($route) {
                $route
                        .when('/soaui', {
                            templateUrl: 'views/soaui.html',
                            controller: 'soaui'})
                        .when('/start', {
                            templateUrl: 'views/start.html',
                            controller: 'start'})
                        .otherwise({
                            redirectTo: '/soaui'
                        })
            }]);

