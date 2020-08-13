package br.com.zenitech.emissorweb.domains;


public class AutorizacoesPinpad {
    private String id;
    private String pedido;
    private String idFromBase;
    private String amount;
    private String requestId;
    private String emailSent;
    private String timeToPassTransaction;
    private String initiatorTransactionKey;
    private String recipientTransactionIdentification;
    private String cardHolderNumber;
    private String cardHolderName;
    private String date;
    private String time;
    private String aid;
    private String arcq;
    private String authorizationCode;
    private String iccRelatedData;
    private String transactionReference;
    private String actionCode;
    private String commandActionCode;
    private String pinpadUsed;
    private String saleAffiliationKey;
    private String cne;
    private String cvm;
    private String balance;
    private String serviceCode;
    private String subMerchantCategoryCode;
    private String entryMode;
    private String cardBrand;
    private String instalmentTransaction;
    private String transactionStatus;
    private String instalmentType;
    private String typeOfTransactionEnum;
    private String signature;
    private String cancellationDate;
    private String capture;
    private String shortName;
    private String subMerchantAddress;
    private String userModel;
    private String isFallbackTransaction;
    private String appLabel;

    public AutorizacoesPinpad(String id, String pedido, String idFromBase, String amount, String requestId, String emailSent, String timeToPassTransaction, String initiatorTransactionKey, String recipientTransactionIdentification, String cardHolderNumber, String cardHolderName, String date, String time, String aid, String arcq, String authorizationCode, String iccRelatedData, String transactionReference, String actionCode, String commandActionCode, String pinpadUsed, String saleAffiliationKey, String cne, String cvm, String balance, String serviceCode, String subMerchantCategoryCode, String entryMode, String cardBrand, String instalmentTransaction, String transactionStatus, String instalmentType, String typeOfTransactionEnum, String signature, String cancellationDate, String capture, String shortName, String subMerchantAddress, String userModel, String isFallbackTransaction, String appLabel) {
        this.id = id;
        this.pedido = pedido;
        this.idFromBase = idFromBase;
        this.amount = amount;
        this.requestId = requestId;
        this.emailSent = emailSent;
        this.timeToPassTransaction = timeToPassTransaction;
        this.initiatorTransactionKey = initiatorTransactionKey;
        this.recipientTransactionIdentification = recipientTransactionIdentification;
        this.cardHolderNumber = cardHolderNumber;
        this.cardHolderName = cardHolderName;
        this.date = date;
        this.time = time;
        this.aid = aid;
        this.arcq = arcq;
        this.authorizationCode = authorizationCode;
        this.iccRelatedData = iccRelatedData;
        this.transactionReference = transactionReference;
        this.actionCode = actionCode;
        this.commandActionCode = commandActionCode;
        this.pinpadUsed = pinpadUsed;
        this.saleAffiliationKey = saleAffiliationKey;
        this.cne = cne;
        this.cvm = cvm;
        this.balance = balance;
        this.serviceCode = serviceCode;
        this.subMerchantCategoryCode = subMerchantCategoryCode;
        this.entryMode = entryMode;
        this.cardBrand = cardBrand;
        this.instalmentTransaction = instalmentTransaction;
        this.transactionStatus = transactionStatus;
        this.instalmentType = instalmentType;
        this.typeOfTransactionEnum = typeOfTransactionEnum;
        this.signature = signature;
        this.cancellationDate = cancellationDate;
        this.capture = capture;
        this.shortName = shortName;
        this.subMerchantAddress = subMerchantAddress;
        this.userModel = userModel;
        this.isFallbackTransaction = isFallbackTransaction;
        this.appLabel = appLabel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPedido() {
        return pedido;
    }

    public void setPedido(String pedido) {
        this.pedido = pedido;
    }

    public String getIdFromBase() {
        return idFromBase;
    }

    public void setIdFromBase(String idFromBase) {
        this.idFromBase = idFromBase;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(String emailSent) {
        this.emailSent = emailSent;
    }

    public String getTimeToPassTransaction() {
        return timeToPassTransaction;
    }

    public void setTimeToPassTransaction(String timeToPassTransaction) {
        this.timeToPassTransaction = timeToPassTransaction;
    }

    public String getInitiatorTransactionKey() {
        return initiatorTransactionKey;
    }

    public void setInitiatorTransactionKey(String initiatorTransactionKey) {
        this.initiatorTransactionKey = initiatorTransactionKey;
    }

    public String getRecipientTransactionIdentification() {
        return recipientTransactionIdentification;
    }

    public void setRecipientTransactionIdentification(String recipientTransactionIdentification) {
        this.recipientTransactionIdentification = recipientTransactionIdentification;
    }

    public String getCardHolderNumber() {
        return cardHolderNumber;
    }

    public void setCardHolderNumber(String cardHolderNumber) {
        this.cardHolderNumber = cardHolderNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getArcq() {
        return arcq;
    }

    public void setArcq(String arcq) {
        this.arcq = arcq;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getIccRelatedData() {
        return iccRelatedData;
    }

    public void setIccRelatedData(String iccRelatedData) {
        this.iccRelatedData = iccRelatedData;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getCommandActionCode() {
        return commandActionCode;
    }

    public void setCommandActionCode(String commandActionCode) {
        this.commandActionCode = commandActionCode;
    }

    public String getPinpadUsed() {
        return pinpadUsed;
    }

    public void setPinpadUsed(String pinpadUsed) {
        this.pinpadUsed = pinpadUsed;
    }

    public String getSaleAffiliationKey() {
        return saleAffiliationKey;
    }

    public void setSaleAffiliationKey(String saleAffiliationKey) {
        this.saleAffiliationKey = saleAffiliationKey;
    }

    public String getCne() {
        return cne;
    }

    public void setCne(String cne) {
        this.cne = cne;
    }

    public String getCvm() {
        return cvm;
    }

    public void setCvm(String cvm) {
        this.cvm = cvm;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getSubMerchantCategoryCode() {
        return subMerchantCategoryCode;
    }

    public void setSubMerchantCategoryCode(String subMerchantCategoryCode) {
        this.subMerchantCategoryCode = subMerchantCategoryCode;
    }

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getInstalmentTransaction() {
        return instalmentTransaction;
    }

    public void setInstalmentTransaction(String instalmentTransaction) {
        this.instalmentTransaction = instalmentTransaction;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getInstalmentType() {
        return instalmentType;
    }

    public void setInstalmentType(String instalmentType) {
        this.instalmentType = instalmentType;
    }

    public String getTypeOfTransactionEnum() {
        return typeOfTransactionEnum;
    }

    public void setTypeOfTransactionEnum(String typeOfTransactionEnum) {
        this.typeOfTransactionEnum = typeOfTransactionEnum;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(String cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public String getCapture() {
        return capture;
    }

    public void setCapture(String capture) {
        this.capture = capture;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSubMerchantAddress() {
        return subMerchantAddress;
    }

    public void setSubMerchantAddress(String subMerchantAddress) {
        this.subMerchantAddress = subMerchantAddress;
    }

    public String getUserModel() {
        return userModel;
    }

    public void setUserModel(String userModel) {
        this.userModel = userModel;
    }

    public String getIsFallbackTransaction() {
        return isFallbackTransaction;
    }

    public void setIsFallbackTransaction(String isFallbackTransaction) {
        this.isFallbackTransaction = isFallbackTransaction;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }
}
