"use strict";

var main_app = angular.module('main-app', []);


main_app.controller('AppCtrl', function($scope, $http) {

    $scope.errors = "";
    $scope.link_generate = false;
    $scope.tags_fields = [1];
    $scope.our_tags = [];

    $scope.testing_data = function(site_url) {
        console.log($scope.our_tags);
        $http({
            method: 'POST',
            url: '/test/',
            data: {siteUrl: site_url, htmlTags: $scope.our_tags},
            headers: {'Content-Type': 'application/json'}
        }).success(function(answ){
            console.log(answ);
            $scope.errors = "";
            $scope.link_generate = false;
            $scope.tesing_answ = JSON.stringify(answ);
        })
        .error(function(answ){
            console.log(answ);
            $scope.link_generate = false;
            $scope.errors = answ;
        });

    };

    $scope.add_new_tag_field = function() {
        $scope.tags_fields.push(1);
        console.log($scope.tags_fields);
    };

    $scope.generate_link = function(site_url, html_tags) {
        $http({
            method: 'POST',
            url: '/generate_link/',
            data: {siteUrl: site_url, htmlTags: $scope.our_tags},
            headers: {'Content-Type': 'application/json'}
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
