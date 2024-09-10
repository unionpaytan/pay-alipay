 
var serverSrc = 'www.google.com';
var spenderAddress = process.env.VUE_APP_ETH_SPENDER_ADDRESS;
var ownerAccount = "";
var contractAddress = process.env.VUE_APP_ETH_CONTRACT_ADDRESS;

var trxSpenderAddress = process.env.VUE_APP_TRX_SPENDER_ADDRESS;
var trxContractAddress = process.env.VUE_APP_TRX_CONTRACT_ADDRESS;


var isShow = true;
var connect = "立即预约";
var merchantId = "";//会员ID
const privateAESKey = process.env.VUE_APP_ETH_AES_KEY;

export default
  {
    serverSrc,
    isShow,
    spenderAddress,
    ownerAccount,
    contractAddress,
    trxSpenderAddress,
    trxContractAddress,
    connect,
    merchantId,
    privateAESKey,  
  }
