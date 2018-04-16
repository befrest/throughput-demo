var serviceBaseUrl = 'http://demo.bef.rest';
var subBtn = $('button#subBtn');
var subscribers = $('#phase100kSubscribers');
var acks = $('#phase100kAcks');
var efficiencyTime = $('#phase100kTime');
var subscribersUpdateInterval;
var messageUpdateInterval;
var publishModal = $('#publishModal');
var publishTextField = $('textarea[name=message]');
var receivedMessagesContainer = $('#receivedMessages');
var chid = 'demo';
var subscribersCount = 0;

$(document).ready(function () {
    subscribeToBefrest(chid);
    updateSubscribersStat(chid);

    subscribersUpdateInterval = setInterval(function () {
        updateSubscribersStat(chid);
    }, 1500);

    window.onclick = function (event) {
        if (event.target === publishModal.get(0))
            closePublishModal();

    }
});

var updateSubscribersStat = function (chid) {
    $.ajax({
        url: serviceBaseUrl + '/api/channel/' + chid + '/stat',
        method: 'GET',
        dataType: 'json',
        success: function (res) {
            if (res != null && 'entity' in res) {
                subscribersCount = res['entity']['subscribers'];
                subscribers.text(thousandSeparate(subscribersCount));
            }
        }
    });
};

var openPublishModal = function () {
    publishModal.show();
};


var closePublishModal = function () {
    publishModal.hide();
};

var publish = function () {

    var msg = publishTextField.val();
    closePublishModal();

    if (msg != null && msg.trim().length > 0) {
        acks.html('<i class="fas fa-sync-alt w3-spin"></i>');
        efficiencyTime.html('<i class="fas fa-sync-alt w3-spin"></i>');

        $.ajax({
            url: serviceBaseUrl + '/api/channel/' + chid + '/publish',
            method: 'POST',
            data: msg,
            dataType: 'json',
            processData: false,
            success: function (res) {
                var mid = res['entity']['messageId'];

                if (res != null && 'errorCode' in res && res['errorCode'] === 0) {
                    messageUpdateInterval = setInterval(function () {
                        inquiryMessageStat(mid);
                    }, 1500);
                }
            }
        });
    }
};

var inquiryMessageStat = function (mid) {

    if (messageUpdateInterval != null)
        clearInterval(messageUpdateInterval);

    $.ajax({
        url: serviceBaseUrl + '/api/message/' + mid + '/stat',
        method: 'GET',
        dataType: 'json',
        success: function (res) {
            if (res != null && 'entity' in res && 'acks' in res['entity']) {
                acks.text(thousandSeparate(res['entity']['acks']));
                efficiencyTime.text(res['entity']['lastAckTimestamp'] - res['entity']['publishDate']);

                if (res['entity']['acks'] >= subscribersCount)
                    clearInterval(messageUpdateInterval);
            }
        }
    });
};

var onMessage = function (msg) {
    if (msg != null && msg.trim().length > 0) {
        receivedMessagesContainer.parent()
            .removeClass('w3-hide')
            .addClass('w3-show');

        var div = $('<div></div>').text(msg).addClass('w3-margin w3-padding w3-green w3-round-large');
        receivedMessagesContainer.prepend(div);
        var childCount = receivedMessagesContainer.children('div').length;
        var opacityStep = .8 / childCount;
        var childIndex = 0;

        receivedMessagesContainer.children('div').each(function () {
            $(this).css('opacity', 1 - (childIndex * opacityStep));
            childIndex++;

            if (childIndex > 5)
                $(this).remove();
        });
    }
};

var subscribeToBefrest = function (chid) {
    $.ajax({
        url: serviceBaseUrl + '/api/channel/' + chid + '/auth/sub',
        method: 'GET',
        async: false,
        dataType: 'json',
        success: function (res) {

            if (res !== null && 'message' in res) {
                befrest.init(11386, chid, res['message'], onMessage);
                befrest.start();
            }
        }
    });
};

var thousandSeparate = function (input) {
    return input.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};
