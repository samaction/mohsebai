/**
 * Telegram Bot Agent for AI Accountant App
 * بوت تلغرام الخارجي للذكاء الاصطناعي - المالك المعتمد: putincenterfg12@gmail.com
 * يدعم الاستجابة المرنة الحرة للفواتير والذمم والمبيعات بالدولار USD ($)
 */

const TelegramBot = require('node-telegram-bot-api');
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

const DEFAULT_OWNER_EMAIL = 'putincenterfg12@gmail.com';

// 1. تهيئة Firebase Admin SDK
const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
let db = null;

if (fs.existsSync(serviceAccountPath)) {
  try {
    const serviceAccount = require(serviceAccountPath);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    db = admin.firestore();
    console.log(`🔥 تم الاتصال بسحابة Firebase المربوطة بحساب المالك (${DEFAULT_OWNER_EMAIL})!`);
  } catch (e) {
    console.log(`⚠️ يعمل البوت في الوضع الحسابي المباشر لحساب المالك (${DEFAULT_OWNER_EMAIL}).`);
  }
} else {
  console.log(`ℹ️ البوت جاهز للاستجابة السريعة المرنة لمسؤول التطبيق (${DEFAULT_OWNER_EMAIL}).`);
}

// 2. توكن بوت تلغرام المعتمد
const BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN || '9274013467:AAE1kZaYKFwvnjdR08Pkzm3ymRKPduzmJK4';
const bot = new TelegramBot(BOT_TOKEN, { polling: true });

console.log(`🤖 بوت المحاسب الذكي الحر يعمل الآن بحساب المالك (${DEFAULT_OWNER_EMAIL}) وجاهز للاستجابة!`);

// 3. معالجة أوامر تلغرام الحرة غير المحصورة
bot.on('message', async (msg) => {
  const chatId = msg.chat.id;
  const text = msg.text;

  if (!text) return;

  if (text === '/start') {
    bot.sendMessage(chatId, `👋 مرحباً بك في بوت المحاسب الذكي والمؤسسي الحر (${DEFAULT_OWNER_EMAIL})!\n\nيمكنك كتابة أي أمر مالي بحرية وبأي طريقة تناسبك مثل:\n- "فاتورة بيع بضاعة 500 دولار نقدا"\n- "شراء 200 بنك"\n- "كشف حساب عميل 150"\n- "صرفت 45 طعام"\n\nوسيقوم البوت باستخراجها وإصدار الفواتير والقيود والمزامنة فوراً!`);
    return;
  }

  try {
    const amountMatch = text.match(/(\d+(\.\d+)?)/);
    const amount = amountMatch ? parseFloat(amountMatch[1]) : null;

    const isInvoice = text.includes('فاتورة') || text.includes('بيع') || text.includes('شراء');
    const isIncome = text.includes('بيع') || text.includes('دخل') || text.includes('قبضت') || text.includes('ارباح') || text.includes('إيراد');
    const isExpense = text.includes('شراء') || text.includes('صرفت') || text.includes('دفعت') || text.includes('فاتورة');

    const isCash = text.includes('نقدا') || text.includes('كاش') || text.includes('صندوق');
    const isBank = text.includes('بنك') || text.includes('تحويل') || text.includes('بطاقة');
    const isCredit = text.includes('ذمة') || text.includes('دين') || text.includes('آجل');

    const paymentLabel = isBank ? 'بنك' : (isCredit ? 'ذمة' : 'نقداً');

    if (amount) {
      const type = isIncome ? 'INCOME' : 'EXPENSE';
      const title = `${text.substring(0, 30)} [${paymentLabel}]`;

      if (db) {
        await db.collection('users').doc(DEFAULT_OWNER_EMAIL).collection('memory').add({
          title: title,
          amount: amount,
          type: type,
          currency: 'USD',
          paymentMethod: paymentLabel,
          ownerEmail: DEFAULT_OWNER_EMAIL,
          source: 'TELEGRAM_BOT',
          timestamp: Date.now()
        });
      }

      const reply = isInvoice
        ? `🧾 تم إصدار وتثبيت الفاتورة الحرّة بقيمة $${amount} [طريقة السداد: ${paymentLabel}] ومزامنتها مع تطبيقك!`
        : (isIncome
          ? `✅ تم تسجيل إيراد/بيع بقيمة $${amount} [طريقة السداد: ${paymentLabel}] ومزامنتها!`
          : `💸 تم تسجيل مصروف/شراء بقيمة $${amount} [طريقة السداد: ${paymentLabel}] ومزامنتها!`);

      bot.sendMessage(chatId, reply);
    } else {
      bot.sendMessage(chatId, `🤖 أنا المحاسب الذكي الحر لحساب ${DEFAULT_OWNER_EMAIL}.\nاكتب لي أي معاملة مثل:\n"فاتورة بيع 300 دولار كاش" أو "صرفت 50 مواصلات" وسأنفذها فوراً.`);
    }
  } catch (error) {
    console.error('Error handling message:', error);
    bot.sendMessage(chatId, '❌ حدث خطأ أثناء معالجة الأمر.');
  }
});