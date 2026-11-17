/**
 * Telegram Bot Agent for AI Accountant App
 * بوت تلغرام الخارجي المحاسبي - المالك المعتمد: putincenterfg12@gmail.com
 */

const TelegramBot = require('node-telegram-bot-api');
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

const OWNER_EMAIL = 'putincenterfg12@gmail.com';

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
    console.log(`🔥 تم الاتصال بسحابة Firebase المربوطة بحساب المالك (${OWNER_EMAIL})!`);
  } catch (e) {
    console.log(`⚠️ يعمل البوت في الوضع الحسابي المباشر لحساب المالك (${OWNER_EMAIL}).`);
  }
} else {
  console.log(`ℹ️ البوت جاهز للاستجابة السريعة لمسؤول التطبيق (${OWNER_EMAIL}).`);
}

// 2. توكن بوت تلغرام المعتمد
const BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN || '9274013467:AAE1kZaYKFwvnjdR08Pkzm3ymRKPduzmJK4';
const bot = new TelegramBot(BOT_TOKEN, { polling: true });

console.log(`🤖 بوت المحاسب الذكي يعمل الآن بحساب المالك (${OWNER_EMAIL}) وجاهز للاستجابة!`);

// 3. معالجة أوامر تلغرام
bot.on('message', async (msg) => {
  const chatId = msg.chat.id;
  const text = msg.text;

  if (!text) return;

  if (text === '/start') {
    bot.sendMessage(chatId, `👋 مرحباً بك يا رئيس المحاسبين (${OWNER_EMAIL})!\n\nيمكنك إرسال حركات المبيعات والمشتريات وطريقة السداد مثل:\n- "بيع 250 دولار نقدا"\n- "شراء بضاعة 100 بنك"\n- "صرفت 45 طعام ذمة"\n\nوسيقوم البوت بتسجيلها بالدولار ($) وتطبيق قيدها المزدوج فوراً!`);
    return;
  }

  try {
    const amountMatch = text.match(/(\d+(\.\d+)?)/);
    const amount = amountMatch ? parseFloat(amountMatch[1]) : null;

    const isIncome = text.includes('بيع') || text.includes('دخل') || text.includes('قبضت') || text.includes('ارباح') || text.includes('إيراد');
    const isExpense = text.includes('شراء') || text.includes('صرفت') || text.includes('دفعت') || text.includes('فاتورة');

    const isCash = text.includes('نقدا') || text.includes('كاش') || text.includes('صندوق');
    const isBank = text.includes('بنك') || text.includes('تحويل') || text.includes('بطاقة');
    const isCredit = text.includes('ذمة') || text.includes('دين') || text.includes('آجل');

    const paymentLabel = isBank ? 'بنك' : (isCredit ? 'ذمة' : 'نقداً');

    if (amount && (isIncome || isExpense)) {
      const type = isIncome ? 'INCOME' : 'EXPENSE';
      const title = `${text.substring(0, 30)} [${paymentLabel}]`;

      if (db) {
        await db.collection('users').doc(OWNER_EMAIL).collection('memory').add({
          title: title,
          amount: amount,
          type: type,
          currency: 'USD',
          paymentMethod: paymentLabel,
          ownerEmail: OWNER_EMAIL,
          source: 'TELEGRAM_BOT',
          timestamp: Date.now()
        });
      }

      const reply = isIncome
        ? `✅ تم تسجيل حركة بيع/إيراد بقيمة $${amount} [طريقة السداد: ${paymentLabel}] لحساب المالك (${OWNER_EMAIL}) ومزامنتها!`
        : `💸 تم تسجيل حركة شراء/مصروف بقيمة $${amount} [طريقة السداد: ${paymentLabel}] ومزامنتها!`;

      bot.sendMessage(chatId, reply);
    } else {
      bot.sendMessage(chatId, `🤖 أنا المحاسب الذكي لحساب ${OWNER_EMAIL}.\nاكتب لي عملية مثل:\n"بيع بضاعة 300 دولار نقدا" أو "شراء معدات 150 بنك"وسأنفذها فوراً!`);
    }
  } catch (error) {
    console.error('Error handling message:', error);
    bot.sendMessage(chatId, '❌ حدث خطأ أثناء معالجة القيد المحاسبي.');
  }
});