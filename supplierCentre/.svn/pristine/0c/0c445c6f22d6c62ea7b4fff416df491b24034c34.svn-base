/**
 * 地区选择JS
 * @params container 包含地区选择的父容器，包括省、市、区元素
 */
M.area = function(container){
	var container = $(container);
	if(container.length > 0){
		//获取地区数据
		$.getJSON(ctx + "/common/static/js/area.json", function(areaData){
			if(areaData){
				$(container).each(function(){
					var _this = this;
					var pathStr = $(_this).find("._path_").first().val();
					if(pathStr) {
						pathStr = pathStr.replace('中国,', '');
						var pathArr = pathStr.split(',');
					}
					var _province_select = $(_this).find('.bind-province').first();	// 省下拉框
					var _city_select = $(_this).find('.bind-city');	// 市下拉框 
					var _district_select = $(_this).find('.bind-district');	//区下拉框
					var _is_code = $(_this).attr("isCode") == 'true';  //值是否是area_code;
					if(_province_select.length != 0) {
						var provinceOption = '';
						if($(_province_select).attr('allow-null')){
							provinceOption += '<option value="">请选择</option>';
						}
						for(var province_id in areaData){
							var value = _is_code ? areaData[province_id].area_code : province_id;
							provinceOption += '<option value="'+ value + '" area_id="'+ province_id +'" area_code="'+ areaData[province_id].area_code +'">'+ areaData[province_id].area_name +'</option>';
						}
						$(_province_select).html(provinceOption)	//渲染省下拉框
						if(pathArr && pathArr[0]) {	// 自动定位到给定的省
							$(_province_select).find('option').each(function() {
								if($(this).text() == pathArr[0]) {
									$(_province_select).val($(this).attr('value'));
								}
							});
						}
					}
					
					//渲染城市
					if(_city_select.length != 0) {
						$(_province_select).change(function() {	//给省下拉框绑定change事件，动态获取市
							var province_id = $(_province_select).find("option:checked").attr("area_id");
							if(province_id && province_id != '') {
								var cityData = areaData[province_id].child;
								if(cityData){
									var cityOption = '';
									if($(_city_select).attr('allow-null')) {
										cityOption = '<option value="">请选择</option>' + cityOption;
									}
									for(var city_id in cityData) {
										var value = _is_code ? cityData[city_id].area_code : city_id;
										cityOption += '<option value="'+ value + '" area_id="'+ city_id +'" area_code="'+ cityData[city_id].area_code +'">'+ cityData[city_id].area_name +'</option>';
									}
									$(_city_select).removeAttr('disabled').html(cityOption);	//渲染市下拉框
									if(pathArr && pathArr[1]) {	// 自动定位到给定的市
										$(_city_select).find('option').each(function() {
											if($(this).text() == pathArr[1]) {
												$(_city_select).val($(this).attr('value'));
											}
										});
									}
									$(_city_select).trigger('change');
								} else {
									$(_city_select).empty().attr('disabled', 'disabled');
									$(_district_select).empty().attr('disabled', 'disabled');
								}
							} else {
								$(_city_select).empty().attr('disabled', 'disabled');
								$(_district_select).empty().attr('disabled', 'disabled');
							}
						});
						$(_province_select).trigger("change");	// 第一次主动触发省的change事件
					}
					
					if(_district_select.length != 0) {
						$(_city_select).change(function() {	//给市下拉框绑定change事件，动态获取区
							var province_id = $(_province_select).find("option:checked").attr("area_id");
							var city_id = $(_city_select).find("option:checked").attr("area_id");
							if(city_id && province_id) {
								var districtData = ((areaData[province_id].child)[city_id]).child;
								if(districtData) {
									var districtOption = '';
									if($(_district_select).attr('allow-null')) {
										districtOption += '<option value="">请选择</option>';
									}
									for(var district_id in districtData) {
										var value = _is_code ? districtData[district_id].area_code : district_id;
										districtOption += '<option value="'+ value + '" area_id="'+ district_id +'" area_code="'+ districtData[district_id].area_code +'">'+ districtData[district_id].area_name +'</option>';
									}
									$(_district_select).removeAttr('disabled').html(districtOption);	//渲染区下拉框
									if(pathArr && pathArr[2]) {	// 自动定位到给定的区
										$(_district_select).find('option').each(function() {
											if($(this).text() == pathArr[2]) {
												$(_district_select).val($(this).attr('value'));
											}
										});
									}
								}
							} else {
								$(_district_select).empty().attr('disabled', 'disabled');
							}
						});
						$(_city_select).trigger("change");	// 第一次主动触发市的change事件
					}
				});
			}
		});
	}
}