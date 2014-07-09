var Client = {
	BOSH_SERVICE : 'http://localhost/xmpp-httpbind',
	connection : null,
	room : '',
	server : 'localhost',
	resource : 'webbrowser',
	conference : 'conference.localhost',
	adminBase : 'roomadmin@localhost/',
	admin : '',
	muc_jid : '',
	nickname : '',
	MMZ_URL : 'http://www.mymoviequiz.com',
	NS_MUC : 'http://jabber.org/protocol/muc',

	classementFinalHandler : function(json) {
		var j = $.parseJSON(json);
		var classement = "<ol class='ranking'>";
		for(var i = 0; i < j.length; i++) {
			classement += "<li>" + j[i].nom + " - " + j[i].score + " pts</li>";
		}
		classement += "</ol>";

		$('#gameFrame').html(classement);
		$('#rankingFrame').html('');
	},
	classementHandler : function(json) {
		var j = $.parseJSON(json);
		var classement = "<ol class='ranking'>";
		for(var i = 0; i < j.length; i++) {
			classement += "<li>" + j[i].nom + " - " + j[i].score + " pts</li>";
		}
		classement += "</ol>";
		$('#rankingFrame').html(classement);
		$('#rankingFrame').append("<br/>");
		$('#reponseScore').empty();
	},
	leaveRoom: function(){
		Client.connection.send($pres({
			to: Client.muc_jid,
			type: "unavailable"}));
	},
	mainMessageHandler : function(message) {
		$('#gameFrame').html("<div id='messageServeur'><br/>" + message + "</div>");
	},
	imageReceived : function(imageUrl) {

		var url = Client.MMZ_URL + Strophe.getText(imageUrl);
		$('#gameFrame').html("<img src='" + url + "'/>");

	},
	reponseFilmHandler : function(film) {
		$('#gameFrame').html("<div id='resultat'>La bonne r&eacute;ponse &eacute;tait : <br/>" + film + "</div>");
		$('#reponse').val('');
		$('#reponseServeur').empty();
	},
	reponseResultatReceived : function(reponse) {
		$('#reponseServeur').empty();
		$('#reponseServeur').append(reponse);
	},
	reponseScoreReceived : function(score) {
		$('#reponseScore').empty();
		$('#reponseScore').append('Ton score total est de ' + score);
	},
	messageHandler : function(msg) {

		var elems = msg.getElementsByTagName('body');
		var body = elems[0];
		var property = msg.getElementsByTagName('property')[0];


		if(property != null) {
			var propertyName = property.getElementsByTagName('name')[0];
			var propertyValue = property.getElementsByTagName('value')[0];

			if(propertyName != null) {

				if(Strophe.getText(propertyName) == 'pic') {
					Client.imageReceived(propertyValue);
				} else if(Strophe.getText(propertyName) == 'reponseResultat') {
					Client.reponseResultatReceived(Strophe.getText(propertyValue));
				} else if(Strophe.getText(propertyName) == 'reponseScore') {
					Client.reponseScoreReceived(Strophe.getText(propertyValue));
				} else if(Strophe.getText(propertyName) == 'film') {
					Client.reponseFilmHandler(Strophe.getText(propertyValue));
				} else if(Strophe.getText(propertyName) == 'classement') {
					Client.classementHandler(Strophe.getText(propertyValue));
				} else if(Strophe.getText(propertyName) == 'classementFinal') {
					Client.classementFinalHandler(Strophe.getText(propertyValue));
				} else if(Strophe.getText(propertyName) == 'mainMessage') {
					Client.mainMessageHandler(Strophe.getText(propertyValue));
				}
			}
		}
		return true;
	},
	onConnect : function(status) {
		if(status === Strophe.Status.CONNECTED){
			Client.connection.send($pres().c('priority').t('0'));
			$(document).trigger('connected');
			document.location.href = "/site/game";
		}else if( status === Strophe.Status.ATTACHED) {
			Client.connection.send($pres().c('priority').t('0'));
			$(document).trigger('connected');
			$("#formsContainer").hide();
		} else if(status === Strophe.Status.DISCONNECTED) {
			$(document).trigger('disconnected');
		} else if(status === Strophe.Status.ERROR) {
			alert('erreur lors de la connexion');
		} else if(status === Strophe.Status.CONNFAIL) {
			alert('impossible de se connecter');
		} else if(status === Strophe.Status.AUTHFAIL) {
			alert('utilisateur ou mot de passe incorrect');
			Client.connection = new Strophe.Connection(Client.BOSH_SERVICE);
		}
	},
	autoReconnect : function() {
		Client.connection.disconnect();
		Client.connection.connect($('#login').val() + "@localhost", $('#password').val(), Client.onConnect);
		return true;
	},
	getRooms : function(iq_result) {
		$(iq_result).find('item').each(function() {
			$("#roomList_ul").
			append("<div class='col-xs-12 col-sm-4' >" +
						"<div style=\"background: url('assets/images/"+$(this).attr('name')+".jpg') no-repeat left center;\" class='room_img'>" +
								"<a href='#' class='btn btn-default roomList_li'>" + $(this).attr('name') + "</a>" +
						"</div>" +
					 "</div>");
		});
		$(".roomList_li").click(function() {
			Client.mucConnect($(this).children().text())
		});
		$("#roomList").show();

	},
	mucConnect : function(elem) {
		Client.room = elem;
		Client.muc_jid = Client.room + '@' + Client.conference + '/' + Client.nickname;
		Client.admin = Client.adminBase + capitalise(Client.room);
		Client.connection.send($pres({
			to : Client.muc_jid
		}).c('x', {
			xmlns : Client.NS_MUC
		}));
		Client.initRoom();
	},
	initRoom : function() {
		$("#roomList").hide();
		$("#content").show();
		$("#reponseBouton").click(function() {
			Client.connection.send($msg({
				to : Client.admin
			}).c("properties", {
				xmlns : "http://www.jivesoftware.com/xmlns/xmpp/properties"
			}).c("property").c("name").t("reponse").up().c("value", {
				type : "string"
			}).t($("#reponse").val()));
		});
		Client.muc_admin_jid = Client.room + '@' +Client.conference+"/roomadmin";
		Client.connection.addHandler(Client.messageHandler, null, 'message', 'groupchat', null, Client.muc_admin_jid);
		Client.connection.addHandler(Client.messageHandler, null, 'message', 'groupchat', null, Client.admin);
	}
};

function capitalise(string)
{
    return string.charAt(0).toUpperCase() + string.slice(1);
}

$(document).ready(function() {
	Client.connection = new Strophe.Connection(Client.BOSH_SERVICE);
	
	$(document).bind('connected', function() {
		var iq = $iq({
			to : "conference.localhost",
			type : "get"
		}).c("query", {
			xmlns : 'http://jabber.org/protocol/disco#items'
		});
		
		if(Client.nickname === undefined || "" === Client.nickname){
			Client.nickname = Client.connection.jid.split("@")[0];
		}
		
		sessionStorage.setItem("jid", Client.connection.jid);
		Client.connection.sendIQ(iq, function(iq_result) {
			Client.getRooms(iq_result);
		});
		
		$("#userConnect").text(Client.nickname);
	});
	
	Client.connection.xmlInput = function(traffic) {
		console.log(traffic);
	}
	
	Client.connection.xmlOutput = function(traffic) {
		console.log(traffic);
		if($(traffic).attr("sid"))
			sessionStorage.setItem("sid", $(traffic).attr("sid"));
		if($(traffic).attr("rid"))
			sessionStorage.setItem("rid", $(traffic).attr("rid"));
	}
	$("#signInBouton").click(function() {
		Client.nickname = $('#login').val();
		$.ajax({
			url : '/site/signin',
			data : { username : $('#login').val(), password : $('#password').val()},
			type : 'POST',
			success : function(data) {
				if(data.authenticated === true){
					Client.connection.connect($('#login').val() + "@" + Client.server + '/' + Client.resource, $('#password').val(), Client.onConnect);
				}else{
					alert('utilisateur ou mot de passe incorrect');
				}
			}
		});
		
	});
	$("#signInGuest").click(function() {
		$.ajax({
			url : '/site/joinAsGuest',
			success : function(data) {
				Client.nickname = data.user;
				Client.connection.connect(data.user + "@" + Client.server + '/' + Client.resource, data.password, Client.onConnect);
			}
		});
	});
	
	
	if(sessionStorage.getItem("jid") && sessionStorage.getItem("sid") && sessionStorage.getItem("rid")){
		Client.connection.attach(sessionStorage.getItem("jid"), sessionStorage.getItem("sid"),
				parseInt(sessionStorage.getItem("rid"))+1, Client.onConnect);
	}

	
	
});
