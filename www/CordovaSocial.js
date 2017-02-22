var exec = require('cordova/exec');

exports.loginFb = function(arg0, success, error) {
    exec(success, error, "CordovaSocial", "loginFb", [arg0]);
};
