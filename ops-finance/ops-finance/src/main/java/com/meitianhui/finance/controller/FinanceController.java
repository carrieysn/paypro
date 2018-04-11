package com.meitianhui.finance.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.meitianhui.base.controller.BaseController;
import com.meitianhui.common.constant.PageParam;
import com.meitianhui.common.constant.ResultData;
import com.meitianhui.common.exception.BusinessException;
import com.meitianhui.common.exception.SystemException;
import com.meitianhui.common.util.StringUtil;
import com.meitianhui.common.util.ValidateUtil;
import com.meitianhui.finance.constant.Constant;
import com.meitianhui.finance.constant.RspCode;
import com.meitianhui.finance.service.FinanceService;
import com.meitianhui.finance.service.GoldService;
import com.meitianhui.finance.service.MemberCapitalAccountService;
import com.meitianhui.finance.service.OrderPayService;
import com.meitianhui.finance.service.PrepayCardService;
import com.meitianhui.finance.service.TradeService;
import com.meitianhui.finance.service.VoucherService;

/**
 * 金融服务控制层
 * 
 * @author Tiny
 *
 */
@SuppressWarnings("unchecked")
@Controller
@RequestMapping("/finance")
public class FinanceController extends BaseController {

	@Autowired
	private FinanceService financeService;
	@Autowired
	private MemberCapitalAccountService memberCapitalAccountService;
	@Autowired
	private OrderPayService orderPayService;
	@Autowired
	private TradeService tradeService;
	@Autowired
	private VoucherService voucherService;
	@Autowired
	private GoldService goldService;
	@Autowired
	private PrepayCardService prepayCardService;

	/**
	 * 可缓存线程池
	 */
	public static ExecutorService threadExecutor = Executors.newCachedThreadPool();

	@Override
	public void operate(HttpServletRequest request, HttpServletResponse response, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			String operateName = request.getParameter("service");
			String type = operateName.split("\\.")[0];
			if (type.equals("trade")) {
				tradeServer(request, response, paramsMap, result);
			} else if (type.equals("voucher")) {
				voucherServer(request, response, paramsMap, result);
			} else if (type.equals("mobileRecharge")) {
				mobileRechargeServer(request, response, paramsMap, result);
			} else {
				financeServer(request, response, paramsMap, result);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 礼券
	 * 
	 * @Title: voucherServer
	 * @param request
	 * @param response
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void voucherServer(HttpServletRequest request, HttpServletResponse response, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			String operateName = request.getParameter("service");
			if ("voucher.app.voucherExchangeLogFind".equals(operateName)) {
				voucherService.handleVoucherExchangeLogFind(paramsMap, result);
			} else if ("voucher.app.voucherExchangeGold".equals(operateName)) {
				voucherService.handleVoucherExchangeGold(paramsMap, result);
			} else if ("voucher.app.voucherBalanceFind".equals(operateName)) {
				voucherService.voucherBalanceFind(paramsMap, result);
			} else if ("voucher.app.voucherExchange".equals(operateName)) {
				voucherService.handleVoucherExchange(paramsMap, result);
			} else if ("voucher.stores.voucherExchangeLogPageFind".equals(operateName)) {
				voucherExchangeLogForStoresPageFind(request, paramsMap, result);
			} else {
				throw new BusinessException(RspCode.SYSTEM_SERVICE_ERROR,
						RspCode.MSG.get(RspCode.SYSTEM_SERVICE_ERROR));
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询礼券交易流水
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void voucherExchangeLogForStoresPageFind(HttpServletRequest request, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			ValidateUtil.validateParams(paramsMap, new String[] { "stores_id" });
			paramsMap.put("member_type_key", Constant.MEMBER_TYPE_STORES);
			paramsMap.put("member_id", paramsMap.get("stores_id"));
			paramsMap.remove("stores_id");
			voucherService.voucherExchangeLogForStoresFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 
	 * @Title: mobileRechargeServer
	 * @param request
	 * @param response
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void mobileRechargeServer(HttpServletRequest request, HttpServletResponse response,
			Map<String, Object> paramsMap, ResultData result) throws BusinessException, SystemException, Exception {
		try {
			String operateName = request.getParameter("service");
			if ("mobileRecharge.app.orderPay".equals(operateName)) {
				orderPayService.mobileRechargeOrderPay(paramsMap, result);
			} else {
				throw new BusinessException(RspCode.SYSTEM_SERVICE_ERROR,
						RspCode.MSG.get(RspCode.SYSTEM_SERVICE_ERROR));
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 资产服务
	 * 
	 * @Title: financeServer
	 * @param request
	 * @param response
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void financeServer(HttpServletRequest request, HttpServletResponse response, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			String operateName = request.getParameter("service");
			if ("finance.memberCapitalAccountCreate".equals(operateName)) {
				memberCapitalAccountService.memberCapitalAccountCreate(paramsMap, result);
			} else if ("finance.memberCapitalAccountFind".equals(operateName)) {
				memberCapitalAccountService.memberCapitalAccountFind(paramsMap, result);
			} else if ("finance.memberCapitalAccountEdit".equals(operateName)) {
				memberCapitalAccountService.memberCapitalAccountEdit(paramsMap, result);
			} else if ("finance.memberCapitalAccountDelete".equals(operateName)) {
				memberCapitalAccountService.memberCapitalAccountDelete(paramsMap, result);
			} else if ("finance.memberCapitalAccountApplicationCreate".equals(operateName)) {
				memberCapitalAccountService.memberCapitalAccountApplicationCreate(paramsMap, result);
			} else if ("finance.memberCapitalAccountApplicationApplyListPageFind".equals(operateName)) {
				memberCapitalAccountApplicationApplyListPageFind(request, paramsMap, result);
			} else if ("finance.memberCapitalAccountApplicationListPageFind".equals(operateName)) {
				memberCapitalAccountApplicationListPageFind(request, paramsMap, result);
			} else if ("finance.memberCapitalAccountApplicationEdit".equals(operateName)) {
				memberCapitalAccountService.memberCapitalAccountApplicationEdit(paramsMap, result);
			} else if ("finance.bankListPageFind".equals(operateName)) {
				bankListPageFind(request, paramsMap, result);
			} else if ("finance.prepayCardStatusFind".equals(operateName)) {
				prepayCardService.prepayCardStatusFind(paramsMap, result);
			} else if ("finance.consumerTempPrepayCardCardNoGet".equals(operateName)) {
				prepayCardService.consumerTempPrepayCardCardNoGet(paramsMap, result);
			} else if ("finance.consumerTempPrepayCardCreate".equals(operateName)) {
				prepayCardService.consumerTempPrepayCardCreate(paramsMap, result);
			} else if ("finance.prepayCardActivate".equals(operateName)) {
				prepayCardService.prepayCardActivate(paramsMap, result);
			} else if ("finance.consumerPrepayCardBind".equals(operateName)) {
				prepayCardService.consumerPrepayCardBind(paramsMap, result);
			} else if ("finance.consumerPrepayCardUnBind".equals(operateName)) {
				prepayCardService.consumerPrepayCardUnBind(paramsMap, result);
			} else if ("finance.storesPrepayCardScan".equals(operateName)) {
				prepayCardService.storesPrepayCardScan(paramsMap, result);
			} else if ("finance.prepayCardScan".equals(operateName)) {
				// TODO 2.9.3生效后可以废弃
				prepayCardService.consumerPrepayCardScan(paramsMap, result);
			} else if ("finance.consumerPrepayCardScan".equals(operateName)) {
				// TODO 2.9.3生效后可以废弃
				prepayCardService.consumerPrepayCardScan(paramsMap, result);
			} else if ("finance.consumerPrepayCardFind".equals(operateName)) {
				prepayCardService.consumerPrepayCardFind(paramsMap, result);
			} else if ("finance.consumerPrepayCardEdit".equals(operateName)) {
				prepayCardService.consumerPrepayCardEdit(paramsMap, result);
			} else if ("finance.consumerPrepayCardCount".equals(operateName)) {
				prepayCardService.consumerPrepayCardCount(paramsMap, result);
			} else if ("finance.prepayCardActivateCount".equals(operateName)) {
				prepayCardService.prepayCardActivateCount(paramsMap, result);
			} else if ("finance.prepayCardActivatePageFind".equals(operateName)) {
				prepayCardActivatePageFind(request, paramsMap, result);
			} else if ("finance.transPrepayCardPageFind".equals(operateName)) {
				transPrepayCardPageFind(request, paramsMap, result);
			} else if ("finance.storesActivatePrepayCardFind".equals(operateName)) {
				prepayCardService.storesActivatePrepayCardFind(paramsMap, result);
			} else if ("finance.goldExchangeCash".equals(operateName)) {
				goldService.handleGoldExchangeCash(paramsMap, result);
			} else if ("finance.consumer.memberPointFind".equals(operateName)) {
				financeService.memberPointFind(paramsMap, result);
			} else if ("finance.consumer.memberPointEdit".equals(operateName)) {
				// 修改会员积分
				financeService.memberPointEdit(paramsMap, result);
			} else if ("finance.storeCashierPromotion".equals(operateName)) {
				financeService.storeCashierPromotion(paramsMap, result);
			} else if ("finance.memberAssetQuery".equals(operateName)) {
				financeService.memberAssetFind(paramsMap, result);
			} else if ("finance.memberAssetFind".equals(operateName)) {
				financeService.memberAssetFind(paramsMap, result);
			} else if ("finance.memberAssetListFind".equals(operateName)) {
				financeService.memberAssetListFind(paramsMap, result);
			} else if ("finance.usableCashBalanceFind".equals(operateName)) {
				financeService.memberUsableCashBalanceFind(paramsMap, result);
			} else if ("finance.storeCashCount".equals(operateName)) {
				financeService.storeCashCount(paramsMap, result);
			} else if ("finance.storeVoucherBillCount".equals(operateName)) {
				financeService.storeVoucherBillCount(paramsMap, result);
			} else if ("finance.storeVoucherRewardAccountCount".equals(operateName)) {
				financeService.storeVoucherRewardAccountCount(paramsMap, result);
			} else if ("finance.storesCashierCreate".equals(operateName)) {
				financeService.storesCashierCreate(paramsMap, result);
			} else if ("finance.storesCashierFind".equals(operateName)) {
				financeService.storesCashierFind(paramsMap, result);
			} else if ("finance.storeCashierPage".equals(operateName)) {
				storeCashierPage(request, paramsMap, result);
			} else if ("finance.memberCouponCreate".equals(operateName)) {
				financeService.memberCouponCreate(paramsMap, result);
			} else if ("finance.memberCouponFind".equals(operateName)) {
				financeService.memberCouponFind(paramsMap, result);
			} else if ("finance.memberCouponStatusEdit".equals(operateName)) {
				financeService.memberCouponStatusEdit(paramsMap, result);
			} else if ("finance.memberIdFindBySkuCode".equals(operateName)) {
				financeService.memberIdBySkuCodeFind(paramsMap, result);
			} else if ("finance.memberCouponCount".equals(operateName)) {
				financeService.memberCouponCount(paramsMap, result);
			} else if ("finance.disabledCouponStatusUpdate".equals(operateName)) {
				financeService.disabledCouponStatusUpdate(paramsMap, result);
			} else if ("finance.tradeConsumerListForStores".equals(operateName)) {
				tradeConsumerPageFindForStores(request, paramsMap, result);
			} else if ("finance.tradeConsumerListForMemberList".equals(operateName)) {
				financeService.tradeConsumerListForMemberList(paramsMap, result);
			} else if ("finance.memberCashLogPageFind".equals(operateName)) {
				memberCashLogPageFind(request, paramsMap, result);
			} else if ("finance.memberGoldLogPageFind".equals(operateName)) {
				memberGoldLogPageFind(request, paramsMap, result);
			} else if ("finance.consumer.memberPointLogPageFind".equals(operateName)) {
				// 分页查询会员积分日志
				memberPointLogPageFind(request, paramsMap, result);
			} else if ("finance.memberVoucherLogPageFind".equals(operateName)) {
				memberVoucherLogPageFind(request, paramsMap, result);
			} else if ("finance.storeVoucherBillPage".equals(operateName)) {
				// 店东礼券赠送列表
				storeVoucherBillPage(request, paramsMap, result);
			} else if ("finance.storeVoucherBill".equals(operateName)) {
				financeService.storeVoucherBill(paramsMap, result);
			} else if ("finance.consumerVoucherBill".equals(operateName)) {
				// 运营系统用
				financeService.consumerVoucherBill(paramsMap, result);
			} else if ("finance.memberAssetInit".equals(operateName)) {
				financeService.handleInitMemberAsset(paramsMap, result);
			} else if ("finance.balanceRecharge".equals(operateName)) {
				String member_id = StringUtil.formatStr(paramsMap.get("member_id"));
				if (StringUtils.isEmpty(member_id)) {
					String buyer_id = paramsMap.get("buyer_id") + "";
					paramsMap.put("member_id", buyer_id);
					paramsMap.remove("buyer_id");
					paramsMap.remove("seller_id");
				}
				tradeService.balanceRecharge(paramsMap, result);
			} else if ("finance.balancePay".equals(operateName)) {
				paramsMap.put("out_member_id", paramsMap.get("buyer_id"));
				paramsMap.put("in_member_id", paramsMap.get("seller_id"));
				paramsMap.remove("buyer_id");
				paramsMap.remove("seller_id");
				tradeService.balancePay(paramsMap, result);
			} else if ("finance.orderPay".equals(operateName)) {
				paramsMap.put("out_member_id", paramsMap.get("buyer_id"));
				paramsMap.put("in_member_id", paramsMap.get("seller_id"));
				paramsMap.remove("buyer_id");
				paramsMap.remove("seller_id");
				tradeService.orderPay(paramsMap, result);
			} else if ("finance.orderReward".equals(operateName)) {
				// 买方双方互换位置
				String buyer_id = paramsMap.get("buyer_id") + "";
				String seller_id = paramsMap.get("seller_id") + "";
				paramsMap.put("out_member_id", seller_id);
				paramsMap.put("in_member_id", buyer_id);
				paramsMap.remove("buyer_id");
				paramsMap.remove("seller_id");
				tradeService.orderReward(paramsMap, result);
			} else if ("finance.balanceWithdraw".equals(operateName)) {
				String member_id = StringUtil.formatStr(paramsMap.get("member_id"));
				if (StringUtils.isEmpty(member_id)) {
					String buyer_id = paramsMap.get("buyer_id") + "";
					paramsMap.put("member_id", buyer_id);
					paramsMap.remove("buyer_id");
					paramsMap.remove("seller_id");
				}
				tradeService.balanceWithdraw(paramsMap, result);
			} else if ("finance.balanceWithdrawRefund".equals(operateName)) {
				tradeService.balanceWithdrawRefund(paramsMap, result);
			} else if ("finance.orderRefund".equals(operateName)) {
				// 买方双方互换位置
				String buyer_id = paramsMap.get("buyer_id") + "";
				String seller_id = paramsMap.get("seller_id") + "";
				paramsMap.put("out_member_id", seller_id);
				paramsMap.put("in_member_id", buyer_id);
				paramsMap.remove("buyer_id");
				paramsMap.remove("seller_id");
				tradeService.orderRefund(paramsMap, result);
			} else if ("finance.orderSettlement".equals(operateName)) {
				paramsMap.put("out_member_id", paramsMap.get("buyer_id"));
				paramsMap.put("in_member_id", paramsMap.get("seller_id"));
				paramsMap.remove("buyer_id");
				paramsMap.remove("seller_id");
				tradeService.orderSettlement(paramsMap, result);
			} else if ("finance.assetClear".equals(operateName)) {
				paramsMap.put("member_id", paramsMap.get("seller_id"));
				paramsMap.remove("buyer_id");
				paramsMap.remove("seller_id");
				tradeService.assetClear(paramsMap, result);
			} else if ("finance.transactionReverse".equals(operateName)) {
				tradeService.transactionReverse(paramsMap, result);
			} else if ("finance.transactionConfirmed".equals(operateName)) {
				tradeService.transactionConfirmed(paramsMap, result);
			} else if ("finance.balanceFreeze".equals(operateName)) {
				tradeService.balanceFreeze(paramsMap, result);
			} else if ("finance.balanceUnFreeze".equals(operateName)) {
				tradeService.balanceUnFreeze(paramsMap, result);
			} else if ("finance.transactionStatusFind".equals(operateName)) {
				tradeService.transactionStatusFind(paramsMap, result);
			} else if ("finance.storesUnfreezeBalanceForConsumer".equals(operateName)) {
				financeService.handleStoresUnfreezeBalanceForConsumer(paramsMap, result);
			} else if ("finance.wypOrderPay".equals(operateName)) {
				orderPayService.wypOrderPay(paramsMap, result);
			} else if ("finance.ldOrderPay".equals(operateName)) {
				orderPayService.ldOrderPay(paramsMap, result);
			} else if ("finance.hyd2OrderWebPay".equals(operateName)) {
				orderPayService.hyd2OrderWebPay(request, paramsMap, result);
			} else if ("finance.hyd2OrderScanCodePay".equals(operateName)) {
				orderPayService.hyd2OrderScanCodePay(paramsMap, result);
			} else if ("finance.hyd3OrderWebPay".equals(operateName)) {
				orderPayService.hyd3OrderWebPay(paramsMap, result);
			} else if ("finance.huidianWechatH5Pay".equals(operateName)) {
				orderPayService.huidianWechatH5Pay(paramsMap, result);
			} else if ("finance.huidianWechatPcPay".equals(operateName)) {
				orderPayService.huidianWechatPcPay(paramsMap, result);
			} else if ("finance.miniAppWechatPay".equals(operateName)) {
				//2017-12-2 微信小程序 丁龙
				orderPayService.miniAppWechatPay(paramsMap, result);
			}else if ("finance.tsActivityCreatePay".equals(operateName)) {
				orderPayService.tsActivityCreatePay(paramsMap, result);
			} else if ("finance.tsOrderPay".equals(operateName)) {
				orderPayService.tsOrderPay(paramsMap, result);
			} else if ("finance.billCheckLogListPageFind".equals(operateName)) {
				billCheckLogListPageFind(request, paramsMap, result);
			} else if ("finance.salesassistant.memberAssetFind".equals(operateName)) {
				financeService.memberAssetFind(paramsMap, result);
			} else if ("finance.stores.storesCashCommissionLogListPageFind".equals(operateName)) {
				storesCashCommissionLogListPageFind(request, paramsMap, result);
			} else if ("finance.stores.storesCashCommissionCreate".equals(operateName)) {
				financeService.storesCashCommissionCreate(paramsMap, result);
			} else if ("finance.stores.storesCashCommissionEdit".equals(operateName)) {
				financeService.storesCashCommissionEdit(paramsMap, result);
			} else {
				throw new BusinessException(RspCode.SYSTEM_SERVICE_ERROR,
						RspCode.MSG.get(RspCode.SYSTEM_SERVICE_ERROR) + ";service:" + operateName);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 交易服务
	 * 
	 * @Title: tradeServer
	 * @param request
	 * @param response
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void tradeServer(HttpServletRequest request, HttpServletResponse response, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			String operateName = request.getParameter("service");

			if ("trade.tradeCodeRegister".equals(operateName)) {
				tradeService.tradeCodeRegister(paramsMap, result);
			} else if ("trade.tradeCodeVerify".equals(operateName)) {
				tradeService.tradeCodeVerify(paramsMap, result);
			} else if ("trade.barCodeCreate".equals(operateName)) {
				tradeService.barCodeCreate(paramsMap, result);
			} else if ("trade.barCodePay".equals(operateName)) {
				tradeService.barCodePay(paramsMap, result);
			} else if ("trade.transactionStatusFind".equals(operateName)) {
				tradeService.transactionStatusFind(paramsMap, result);
			} else if ("trade.transactionStatusConfirmed".equals(operateName)) {
				tradeService.transactionStatusConfirmed(paramsMap, result);
			} else if ("trade.salesassistant.barCodePay".equals(operateName)) {
				tradeService.barCodePayForSalesassistant(paramsMap, result);
			} else if ("trade.salesassistant.posPay".equals(operateName)) {
				// pos机支付入口
				tradeService.posPayForSalesassistant(paramsMap, result);
			} else {
				throw new BusinessException(RspCode.SYSTEM_SERVICE_ERROR,
						RspCode.MSG.get(RspCode.SYSTEM_SERVICE_ERROR));
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询会员现金日志
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void memberCashLogPageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.memberCashLogListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询会员积分日志
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void memberPointLogPageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.memberPointLogListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询会员金币日志
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void memberGoldLogPageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.memberGoldLogListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询会员礼券日志
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void memberVoucherLogPageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.memberVoucherLogListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询礼券交易流水
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void storeVoucherBillPage(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.storeVoucherBill(paramsMap, result);
			Map<String, Object> pageData = new HashMap<String, Object>();
			pageData.put("list", result.getResultData());
			pageData.put("page", pageParam);
			result.setResultData(pageData);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询 收银记录
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void storeCashierPage(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.storesCashierFind(paramsMap, result);
			Map<String, Object> pageData = new HashMap<String, Object>();
			pageData.put("list", result.getResultData());
			pageData.put("page", pageParam);
			result.setResultData(pageData);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询亲情卡交易信息
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void transPrepayCardPageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "member_id", "card_no" });
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			prepayCardService.transPrepayCardFind(paramsMap, result);
			Map<String, Object> pageData = new HashMap<String, Object>();
			pageData.put("list", result.getResultData());
			pageData.put("page", pageParam);
			result.setResultData(pageData);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询店东激活的亲情卡信息
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void prepayCardActivatePageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "stores_id" });
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			prepayCardService.prepayCardActivateFind(paramsMap, result);
			Map<String, Object> pageData = new HashMap<String, Object>();
			pageData.put("list", result.getResultData());
			pageData.put("page", pageParam);
			result.setResultData(pageData);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/***
	 * 和店东发生交易的消费者分页列表
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @author 丁硕
	 * @date 2016年10月12日
	 */
	public void tradeConsumerPageFindForStores(HttpServletRequest request, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "stores_id" });
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.tradeConsumerListForStores(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询礼券交易流水
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void bankListPageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			memberCapitalAccountService.bankListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询会员绑定银行卡申请待审核记录查询
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void memberCapitalAccountApplicationApplyListPageFind(HttpServletRequest request,
			Map<String, Object> paramsMap, ResultData result) throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			memberCapitalAccountService.memberCapitalAccountApplicationApplyListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询会员银行绑卡申请信息
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void memberCapitalAccountApplicationListPageFind(HttpServletRequest request, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			memberCapitalAccountService.memberCapitalAccountApplicationListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分页查询交易异常日志
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void billCheckLogListPageFind(HttpServletRequest request, Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.billCheckLogListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 门店的店东的佣金明细列表分页查询
	 * 
	 * @param request
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void storesCashCommissionLogListPageFind(HttpServletRequest request, Map<String, Object> paramsMap,
			ResultData result) throws BusinessException, SystemException, Exception {
		try {
			// 引入分页查询
			PageParam pageParam = getPageParam(request);
			if (null != pageParam) {
				paramsMap.put("pageParam", pageParam);
			}
			financeService.storesCashCommissionLogListFind(paramsMap, result);
			Map<String, Object> resultData = (Map<String, Object>) result.getResultData();
			resultData.put("page", pageParam);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
}
