<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="common.jsp" %>
<body>
 <div class="${laytab eq 'index' ? 'index-header' : ''} ">
  <div class="${laytab eq 'news' ? 'head-alt' : ''}">
  <div class="${laytabs eq 'goodindex' ? 'header-wrap' : ''}"  name="header-wrap" id="header-wrap"> 
  <div class="${laytabs eq 'attractindex' ? 'head-alt' : ''}">
	<div class="header">
		<a href="${ctx}/" class="logo"><c:if test="${laytab eq 'index'}">商家中心</c:if></a>
		<ul class="nav">
			<li class="${laytab eq 'index' ? 'sel' : '' }"><a href="${ctx}/">首页</a></li>
			<li class="${laytab eq 'news' ? 'sel' : '' }"><a href="${ctx}/zxindex">资讯</a></li>
			<li class="${laytab eq 'attract' ? 'sel' : '' }"><a href="${ctx}/zsindex">商家报名</a></li>
			<!-- <li><a href="/partner">供应商登录</a></li> -->
		</ul>
	</div>
 </div>
  </div>
  </div>
</div>
	
<script type="text/javascript">
/* $(function(){
	var lay = ${laytab};
	if(""!= lay && null != lay){
		if(lay == 1){
			$('.nav li').removeClass('sel');
			$('.nav li').eq(1).addClass('sel');
		}
	}
	 $('.nav li').click(function(event) {
		$('.nav li').removeClass('sel');
		$(this).removeClass('sel').addClass('sel');
	});
 }); */
</script>