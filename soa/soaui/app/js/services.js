var app = angular.module('app');

function soatoolsrv(http) {
    this.http = http;
    this.status = function (handle, cb) {
        http({
            method: 'GET',
            url: 'http://localhost:8081/soatoolsrv/status?handle=' + handle
        }).then(function success(response) {
            cb(response);
        }, function problem(response) {
            cb(response);
        });
    };
    this.soatool = function (func,config, cb) {
        http({
            method: 'GET',
            url: 'http://localhost:8081/soatoolsrv/soatool?func=' + func + "&config=" + config
        }).then(function success(response) {
            cb(response);
        }, function problem(response) {
            cb(response);
        });
    };
    this.query = function (func,config, cb) {
        http({
            method: 'GET',
            url: 'http://localhost:8081/soatoolsrv/query/gv?func=' + func + "&config=" + config
        }).then(function success(response) {
            cb(response);
        }, function problem(response) {
            cb(response);
        });
    };
    this.queryInit = function (func,config, cb) {
        http({
            method: 'GET',
            url: 'http://localhost:8081/soatoolsrv/init/gv?config=' + config
        }).then(function success(response) {
            cb(response);
        }, function problem(response) {
            cb(response);
        });
    };
    this.listQueries = function (func,config, cb) {
        http({
            method: 'GET',
            url: 'http://localhost:8081/soatoolsrv/query/list?func=' + func + "&config=" + config
        }).then(function success(response) {
            cb(response);
        }, function problem(response) {
            cb(response);
        });
    };
    this.list = function (cb) {
        http({
            method: 'GET',
            url: 'http://localhost:8081/soatoolsrv/list',
        }).then(function success(response) {
            cb(response);
        }, function problem(response) {
            cb(response);
        });
    };
    this.upload = function (file, cb) {
        http({
            method: 'PUT',
            url: 'http://localhost:8081/soatoolsrv/soatool?func=upload&file=' + file
        }).then(function success(response) {
            cb(response);
        }, function problem(response) {
            cb(response);
        });
    };

}

app.factory('soatoolsrv', ['$http', function ($http) {
        return new soatoolsrv($http);
    }
]);

