var exec = require('cordova/exec');

exports.loginFb = function(success, error) {
    exec(success, error, "CordovaSocial", "loginFb", null);
};