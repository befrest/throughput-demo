var subBtn = $('button#subBtn');

$(document).ready(function () {

});

var updateSubscribersStat = function () {

};

var updateMessagesStat = function () {

};

var inquiryMessageStat = function () {

};

var subscribe = function () {
    $.ajax({
        url: '',
        method: 'POST',
        error: function (err) {

        },
        success: function (res) {
            subBtn.fadeOut();
        }

    });

    return false;
};