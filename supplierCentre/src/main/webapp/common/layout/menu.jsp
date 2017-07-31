<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
	 <!-- menu -->
    <div class="menuContainerWrap">
        <div class="subNavBox" id="_menuContainer_"></div>
    </div>
    <script id="menuTpl" type="text/html">
    	{{each list as menu}}
    	<div class="subNav {{menu.icon_pic_path}}">
    		<a href="{{if menu.isSelected == '0' || menu.action_url == ''}}javascript:void(0);{{else}}{{menu.action_url | appendToken}}{{/if}}" class="{{if menu.menu_id == '${current_menu_id}'}}hover-sel {{/if}}{{if menu.isSelected == '0'}}forbid{{/if}}">
    			<em></em>{{menu.menu_name}}<small></small>
    		</a>
    	</div>
    	{{if menu.child.length != 0}}
        <ul class="navContent">
        	{{each menu.child as sub}}
        		{{if sub.menu_id == 'd454d96c-bde3-11e5-870d-fcaa1490ccaf' || sub.menu_id == 'd47878d5-f32b-11e5-a022-fcaa1490ccaf'}}
        			<li>
        				<a href="{{if sub.isSelected == '0' || sub.action_url == ''}}javascript:void(0);{{else}}{{javascript: window.open('{{sub.action_url}}?token=${token}','newwindow', 'width='+(window.screen.availWidth)+',height='+(window.screen.availHeight-30)+ ',top=0,left=0,toolbar=no,menubar=no,location=no, status=no')}}{{/if}}" 
        					class="{{if sub.menu_id == '${current_menu_id}'}}hover-sel {{/if}}{{if sub.isSelected == '0'}}forbid{{/if}}">{{sub.menu_name}}</a>
        			</li>
        		{{else}}
					<li>
						<a href="{{if sub.isSelected == '0' || sub.action_url == ''}}javascript:void(0);{{else}}{{sub.action_url | appendToken}}{{/if}}" 
							class="{{if sub.menu_id == '${current_menu_id}'}}hover-sel {{/if}}{{if sub.isSelected == '0'}}forbid{{/if}}">{{sub.menu_name}}</a>
					</li>
        		{{/if}}
            {{/each}}
        </ul>
        {{/if}}
        {{/each}}
    </script>
	<div class="main-container-wrap">