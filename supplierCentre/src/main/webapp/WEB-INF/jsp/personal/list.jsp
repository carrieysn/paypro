<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/layout/common.jsp" %>
<body>
    <div class="header-wrap">
	<div class="header">
		<a href="${ctx}/" class="logo"></a>
		<ul class="nav">
			<li><a href="${ctx}/">首页</a></li>
			<li><a href="${ctx}/zxindex">资讯</a></li>
			<li><a href="${ctx}/zsindex">商家报名</a></li>
			<!-- <li><a href="/partner">供应商登录</a></li> -->
		</ul>
	</div>
	</div>
  <div class="process-wrap process-js1">
		<div class="process-rel">
			<div class="step-wrap background-none">
				<div class="step">
					<p class="fl current">每天惠商家中心 > 商家报名 > 商品报名</p>
					<!-- <ul class="step-list">
						<li><i>1</i>商家资料</li>
						<li class="sel"><i>2</i>产品资料</li>
						<li class="sel"><i>3</i>合作方案</li>
					</ul> -->
				</div>
			</div>
			<div class="tab-containe abs-top">
				<div class="tab-wrap">
					<div class="bs-info-wrap clearfix">
						<div class="fl">
							<div class="info-icon fl">
								<img src="${ctx}/common/static/images/mp.png" alt="">
							</div>
							<div class="info-content fl">
								<span>公司名：</span>${sessionUser.supplier_name}</br>
								<span>联系人：</span>${sessionUser.contact_person}</br>
								<span>手机号：</span>${sessionUser.contact_tel}
							</div>
						</div>
						<div class="fr" style="position:absolute;left: 64.8%;">
							<div class="info-icon fl">
								<img src="${ctx}/common/static/images/kf.png" alt="">
							</div>
							<div class="info-content fl">
								<span>客服名：</span>${sessionUser.serviceName}</br>
								<span>手机号：</span>${sessionUser.mobile}</br>
								<span>Q&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Q：</span>
							</div>
						</div>
					</div>
				</div>
				<div class="top20 clearfix">
					<div class="clearfix">
							<ul class="tab-wrap-ul bs-list-wrap bs-listUL clearfix page-list-table" id="presupplier_list_ul" template="presupplierListTpl" url="${ctx}/presupplier/pageList?contact_tel=${sessionUser.contact_tel}" isNullMsg="false" pageSize="10" visiblePages="3"  ></ul>
				      <script id="presupplierListTpl" type="text/html">
			          {{each list as preSupplier i}}
                        <li><a href="${ctx}/presupplier/detail?supplier_id={{preSupplier.supplier_id}}">
                        <div class="bs-img fl"><img src="{{preSupplier.pic_info | imgFormat}}" width="178px" height="178px"></div>
                        <div class="bs-info-text fl">
									<p class="title-p">{{preSupplier.title}}</p>
									<p class="times-p">提交时间：{{preSupplier.created_date}} </p>
						</div>
                        <div class="bs-info-tips fl">类型：{{preSupplier.category}}</div>
                        <div class="bs-info-tips fl">库存：{{preSupplier.stock_qty}}</div>
                        <div class="bs-info-pire fl">
									当前售价：￥<span>{{preSupplier.market_price}}</span></br>
									结算佣金：￥<span>{{preSupplier.settled_price}}</span>
						</div>
                        <div class="ba-indo-zticon fl">
                        {{if preSupplier.audit_status == 'sample'}}<img src="${ctx}/common/static/images/icon-djy.png" alt="">
                        {{else if preSupplier.audit_status == 'review'}}<img src="${ctx}/common/static/images/icon-dfs.png" alt="">
						{{else if preSupplier.audit_status == 'fail'}}<img src="${ctx}/common/static/images/icon-ybh.png" alt="">
		                {{else if preSupplier.audit_status == 'pass'}}<img src="${ctx}/common/static/images/icon-ytg.png" alt="">
                        {{else if preSupplier.audit_status == 'assign'}}<img src="${ctx}/common/static/images/icon-dfp.png" alt="">
                        {{else if preSupplier.audit_status == 'trial'}}<img src="${ctx}/common/static/images/icon-dcs.png" alt="">
					    {{/if}}
                        </div>
                        <div class="ba-link-icon fr">
									<strong>
										<img src="${ctx}/common/static/images/icon-r.png" alt="">
									</strong>
						</div>
						</a>
                        </li>
			            {{/each}}
		                </script>
					</div>
				</div>
			<!-- 	<div class="tab-pages clearfix">
					共 <strong>5</strong> 条记录 
					<select>
  						<option value="1">1</option>
  						<option value="2">2</option>
  						<option value="3">3</option>
  						<option value="4">4</option>
  					</select> <a href="">首页</a> <a href="">上一页</a> <a href="" class="cur">1</a> <a href="">2</a> <a href="">3</a> ... <a href="">下一页</a> <a href="">尾页</a> 1/3 <input type="text" value="4"> <input type="submit" value="GO" class="btn" />
				</div> -->
			</div>
		</div>
	</div>

	
		<script type="text/javascript">
		$(function(){
			template.config("escape", false);
			template.helper('labelFormat', function (label) {
				if(label){
					var label = label.split(/[,， 　]/);
					//var label = label.split(",");
					var labelHtml = "";
					$.each(label, function(index, value){
						if(label[index]){
						   labelHtml += ''+label[index] + '&nbsp;';
						}
					});
					return labelHtml;
				}
			});
			
			//格式化图片信息
			template.helper('imgFormat', function (pic_info) {
				if(pic_info){
					var pic_info = $.parseJSON(pic_info);
					if($.isArray(pic_info) && pic_info[0]){
						return '${ctx}/file/preview.do?doc_id=' + pic_info[0].path_id;
					}
				}
				return '';
			});
			
			
		});
		
		</script>
<%-- 	<script type="text/javascript" src="${ctx}/common/static/goods/js/list.js"></script> --%>
<%@ include file="/common/layout/footer.jsp"%>
    