"use strict";

var main_app = angular.module('main-app', []);


main_app.controller('AppCtrl', function($scope, $http) {

    $scope.errors = "";
    $scope.link_generate = false;

    $scope.testing_data = function(site_url, html_tags) {
        $http({
            method: 'POST',
            url: '/test/',
            data: $.param({siteUrl: site_url, htmlTags: html_tags}),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function(answ){
            console.log(answ);
            $scope.errors = "";
            $scope.link_generate = false;
            $scope.tesing_answ = answ;
        })
        .error(function(answ){
            console.log(answ);
            $scope.link_generate = false;
            $scope.errors = answ;
        });

    };

    $scope.generate_link = function(site_url, html_tags) {
        $http({
            method: 'POST',
            url: '/generate_link/',
            data: $.param({siteUrl: site_url, htmlTags: html_tags}),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function(answ){
            console.log(answ);
            $scope.errors = "";
            $scope.link_generate = true;
            $scope.link_of = answ;
        })
        .error(function(answ){
            console.log(answ);
            $scope.link_generate = false;
            $scope.errors = answ;
        });

    };


});
