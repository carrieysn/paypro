$(function() {
	//请求地址
	var _portal_server_ = portal_server;
	if(_portal_server_.startsWith("https:")){
		_portal_server_ = _portal_server_.substring(6)
	} else if(_portal_server_.startsWith("http:")){
		_portal_server_ = _portal_server_.substring(5)
	}
	
    //对地址栏拼接参数
    template.helper('appendToken', function (url){
    	return url + (url.indexOf('?') > -1 ? '&' : '?') + 'token=' + token
	});
    
 // 点击消息
    $('#new-h').click(function() {
        $('.news-ul').toggle();
        $('.log-ul').hide();
    });

    // 点击用户名
    $('#log-h').click(function() {
        $('.log-ul').toggle();
        $('.news-ul').hide();
    });

    // 动态绑定一级菜单点击事件
    $('#_menuContainer_').on('click', '.subNav', function() {
    	$(".subNav").children('a').removeClass('hover-sel');
        $(this).toggleClass("currentDt").siblings(".subNav").removeClass("currentDt");
        $(this).next(".navContent").slideToggle(300).siblings(".navContent").slideUp(300);
        if(!$(this).children('a').hasClass('forbid')) {
            $(this).children('a').addClass('hover-sel');
        }
    });
    
    // jsonp加载用户数据和菜单
    $.getJSON(_portal_server_ + '/omp-mth-api/userAccount?service=account.queryUserInfo&jsoncallback=?&params=' + JSON.stringify({token: token}), function(data){
    	var userInfo = data.data;
    	// 写入用户名和组织
    	$('#_user_name_').text(userInfo.name);
    	$('#_user_team_').text(userInfo.team.team_name);

    	// 渲染菜单
    	var menuHtml = template('menuTpl', {list: userInfo.menuList});
    	$('#_menuContainer_').html(menuHtml);
        $('#_menuContainer_').slimScroll({
            width: '150px',
            height: $(window).height()-60,
            size: '0'
        });
        
        //菜单自动展开
        var _hover_sel_li_a_= $("#_menuContainer_ .navContent .hover-sel");
        if(_hover_sel_li_a_.length > 0){
        	var subNav = $(_hover_sel_li_a_).closest("ul").prev(".subNav");
        	subNav.children('a').addClass("hover-sel");
        	subNav.next(".navContent").show();
        }
        //将页面滚动到指定菜单位置
        var scrollTarget =  $('#_menuContainer_ .hover-sel').last();
        if(scrollTarget.length > 0){
        	var diff = scrollTarget.offset().top - $('#_menuContainer_').height();
        	if(diff  > 0){
        		$('#_menuContainer_').scrollTop(diff + 100);
        	}
        }
    });
    
    //修改密码
    var _changePasswordForm = $('#_changePasswordForm_').Validform({
		tiptype: 3
	});
    $("#_change_pwd_a").click(function(){
    	layer.open({
			title:  ['修改密码', 'font-size:15px;font-weight: bold;'],
			type: 1,
			icon: 1,
		    btn: ['确定', '取消'],
			shadeClose: true,
			shade: 0.8,
			zIndex: 98,
			shadeClose: false,
			area: ['460px', '280px'],
			content: $("#_password-info-div"),
			yes: function(index){
				if(_changePasswordForm.check(false)){
					var loading = layer.load();
					var params = $.extend(M.formatElement('#_changePasswordForm_'),{token: token});
					$.getJSON(_portal_server_ + '/omp-mth-api/userAccount?service=account.changePwd&jsoncallback=?&params=' + JSON.stringify(params), function(result){
						layer.close(loading);
						if(result.rsp_code == 'succ'){
							M.alert("密码修改成功！", function(){
								layer.close(index);
							});
						} else{
							M.alert(result.error_msg || "密码修改失败！");
						}
					});
				}
			},
			cancel: function(index){
		    	layer.close(index);
		    },
		    end: function(index){
		    	_changePasswordForm.resetForm();	//重置表单
		    }
		});
    });
});

//用户登出
function _logout() {
    window.location.href = '/portal/logout?token=' + token;
}