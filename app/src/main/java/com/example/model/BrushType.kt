package com.example.model

import androidx.compose.ui.graphics.Color

enum class BrushCategory(val titleAr: String, val icon: String) {
    SKETCH("تخطيط ورسم", "✏️"),
    PAINT("ألوان ودهان", "🎨"),
    SPLATTER("تناثر وخامات", "🖌️"),
    FX("تأثيرات وإضاءة", "✨"),
    UTILITY("أدوات وممحاة", "🧹")
}

enum class BrushType(
    val titleAr: String,
    val descriptionAr: String,
    val category: BrushCategory,
    val defaultSize: Float,
    val defaultOpacity: Float
) {
    // 1. Sketch & Inking
    PEN("تحبير دقيق", "خط حاد وثابت مناسب للتفاصيل والمانجا", BrushCategory.SKETCH, 6f, 1.0f),
    PENCIL("قلم رصاص 2B", "تظليل جرافيتي ناعم عالي الحساسية", BrushCategory.SKETCH, 4f, 0.85f),
    MECHANICAL_PENCIL("قلم سنون 0.5", "خطوط رفيعة جداً للمخططات الأولية", BrushCategory.SKETCH, 2.5f, 0.90f),
    STUDIO_PEN("قلم استوديو", "تحبير انسيابي عالي النعومة والرشاقة", BrushCategory.SKETCH, 7f, 1.0f),
    TECHNICAL_PEN("قلم تقني", "سماكة موحدة بدون تدرج للرسم الدقيق", BrushCategory.SKETCH, 3f, 1.0f),
    CALLIGRAPHY("خط عربي", "ريشة مشطوفة بزاوية 45 درجة للخطوط التشكيلية", BrushCategory.SKETCH, 16f, 1.0f),
    COMIC_INKER("حبر كوميكس", "خط ديناميكي يتأثر بالسرعة والضغط", BrushCategory.SKETCH, 9f, 1.0f),

    // 2. Paint & Acrylic (Brusheezy Collection)
    ACRYLIC_WET("أكريليك رطب", "طلاء سميك رطب يحاكي ألوان الأكريليك", BrushCategory.PAINT, 28f, 0.88f),
    OIL("زيتي كلاسيكي", "ألوان زيتية مشبعة مع وميض لمعة خفيف", BrushCategory.PAINT, 32f, 0.92f),
    OIL_IMPASTO("إمباستو كثيف", "طبقات طلاء بارزة الملمس بضربات بارزة", BrushCategory.PAINT, 36f, 0.95f),
    DRY_BRISTLE("شعر خشن جاف", "شعيرات جافة تترك خطوط مسامية فنية", BrushCategory.PAINT, 26f, 0.75f),
    PALETTE_KNIFE("سكين مسطح", "أطراف حادة ومساحات لونية مفرودة كالأصل", BrushCategory.PAINT, 34f, 0.95f),
    WET_SPONGE("إسفنجة رطبة", "توزيع لوني عشوائي ذو مسامات رطبة", BrushCategory.PAINT, 42f, 0.60f),
    WATERCOLOR("ألوان مائية", "صبغة شفافة مع ترسبات مائية على الأطراف", BrushCategory.PAINT, 36f, 0.45f),
    WATERCOLOR_BLEED("مائي متسرب", "تداخل لوني سلس وتمدد سائل", BrushCategory.PAINT, 44f, 0.35f),
    GOUACHE_OPAQUE("جواش معتم", "طلاء صلب غير شفاف ذو لمعة مطفية", BrushCategory.PAINT, 30f, 0.96f),
    SUMI_E("حبر سومي إي", "حبر شرقي تقليدي يتراوح بين الأسود والشفاف", BrushCategory.PAINT, 22f, 0.85f),

    // 3. Splatters & Textures (Brusheezy Collection)
    PAINT_SPLATTER("تناثر طلاء", "رذاذ وبقع طلاء متناثرة حقيقية كاستوديو الفن", BrushCategory.SPLATTER, 45f, 0.85f),
    INK_DRIP("تنقيط حبر", "قطرات حبر مسكوبة متناثرة على اللوحة", BrushCategory.SPLATTER, 38f, 0.90f),
    SPRAY_PAINT("بخاخ غرافيتي", "رذاذ طلاء جداري محاط بذرات غبار ملونة", BrushCategory.SPLATTER, 50f, 0.70f),
    GRUNGE_ROLLER("رولر خامات", "ملمس خشن يحاكي الجدران القديمة", BrushCategory.SPLATTER, 48f, 0.65f),
    STIPPLE("تنقيط تظليلي", "تجمعات نقطية دقيقة للتظليل الكلاسيكي", BrushCategory.SPLATTER, 20f, 0.80f),
    CHARCOAL("فحم كربوني", "مسحوق فحم ناعم بحواف ضبابية", BrushCategory.SPLATTER, 24f, 0.75f),
    SOFT_PASTEL("باستيل طباشيري", "ملمس طباشيري حريري قابل للمزج", BrushCategory.SPLATTER, 30f, 0.70f),

    // 4. Special FX & Lights
    AIRBRUSH("بخاخ ناعم", "تدرج هوائي فائق النعومة للظلال والضوء", BrushCategory.FX, 55f, 0.30f),
    MARKER("ماركر تظليل", "تظليل شفاف بحواف هندسية", BrushCategory.FX, 28f, 0.65f),
    NEON("نيون مضيء", "توهج فوتوني خارجي وقلب أبيض مشع", BrushCategory.FX, 14f, 1.0f),
    BOKEH("دوائر بوكيه", "كرات ضوئية ضبابية تعطي عمقاً وتألقاً", BrushCategory.FX, 46f, 0.65f),
    SPARKLES("بريق ماسي", "تألقات نجمية رباعية براقة", BrushCategory.FX, 26f, 0.90f),
    STARS("نجوم فلكية", "نجوم خماسية متناثرة في مسار الفرشاة", BrushCategory.FX, 30f, 0.95f),
    CLOUD_MIST("غيوم وضباب", "تكتلات بخار وسحب ناعمة", BrushCategory.FX, 60f, 0.28f),
    HAIR_FUR("خصلات شعر", "خطوط متوازية متعددة ترسم الشعر والفرو دفعة واحدة", BrushCategory.FX, 32f, 0.80f),

    // 5. Utility
    SMUDGE("دمج ومزج", "مزج الألوان وتنعيم الحواف المشتركة", BrushCategory.UTILITY, 32f, 0.60f),
    ERASER("ممحاة دقيقة", "مسح نظيف وحاد لكافة الطبقات أو الطبقة النشطة", BrushCategory.UTILITY, 24f, 1.0f),
    ERASER_SOFT("ممحاة ناعمة", "مسح تدريجي متدرج الحواف بدون قطع حاد", BrushCategory.UTILITY, 40f, 0.50f)
}

data class BrushConfig(
    val type: BrushType = BrushType.PEN,
    val size: Float = 8f,
    val opacity: Float = 1.0f,
    val color: Color = Color.White,
    val smoothing: Float = 0.65f,
    val zeroLatency: Boolean = true
)
