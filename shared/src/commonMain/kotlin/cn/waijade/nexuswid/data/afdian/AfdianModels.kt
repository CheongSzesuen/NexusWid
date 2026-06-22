package cn.waijade.nexuswid.data.afdian

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object FlexibleIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val input = decoder as? JsonDecoder ?: return decoder.decodeInt()
        val element = input.decodeJsonElement()
        return element.jsonPrimitive.content.toIntOrNull() ?: 0
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}

@Serializable
data class AfdianDashboardResponse(
    val ec: Int,
    val em: String,
    val data: AfdianDashboardData? = null
)

@Serializable
data class AfdianDashboardData(
    val summary: AfdianSummary? = null
)

@Serializable
data class AfdianSummary(
    val all_sum_amount: String = "0.00",
    val month_amount: String = "0.00",
    @Serializable(with = FlexibleIntSerializer::class)
    val all_sponsor_count: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val month_sponsor_count: Int = 0
)

data class AfdianEarnings(
    val totalAmount: Double,
    val totalCount: Int,
    val monthlyAmount: Double,
    val monthlyCount: Int
)

@Serializable
data class AfdianCheckResponse(
    val ec: Int,
    val em: String,
    val data: AfdianCheckData? = null
)

@Serializable
data class AfdianCheckData(
    @Serializable(with = FlexibleIntSerializer::class)
    val unread_message_num: Int = 0,
    val unread_count: AfdianUnreadBreakdown? = null
)

@Serializable
data class AfdianUnreadBreakdown(
    @Serializable(with = FlexibleIntSerializer::class)
    val comment: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val like: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val message: Int = 0
)

data class AfdianUnreadCount(
    val total: Int,
    val comment: Int,
    val like: Int,
    val message: Int
)

@Serializable
data class AfdianPlanResponse(
    val ec: Int,
    val em: String,
    val data: AfdianPlanData? = null
)

@Serializable
data class AfdianPlanData(
    val list: List<AfdianPlan>? = null,
    val sale_list: List<AfdianPlan>? = null
)

@Serializable
data class AfdianPlan(
    val plan_id: String,
    val name: String = "",
    val status: Int = 0,
    val price: String = "0.00",
    val total_amount: String = "0.00",
    @Serializable(with = FlexibleIntSerializer::class)
    val sponsor_count: Int = 0,
    val pic: String = ""
)

data class AfdianProductSummary(
    val planId: String,
    val name: String,
    val totalAmount: Double,
    val sponsorCount: Int,
    val price: String
)

@Serializable
data class AfdianStatResponse(
    val ec: Int,
    val em: String,
    val data: AfdianStatData? = null
)

@Serializable
data class AfdianStatData(
    val list: List<AfdianDailyStat> = emptyList(),
    @Serializable(with = FlexibleIntSerializer::class)
    val has_more: Int = 0
)

@Serializable
data class AfdianDailyStat(
    @Serializable(with = FlexibleIntSerializer::class)
    val date_str: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val uv: Int = 0,
    val paid_order_real_amount: String = "0.00",
    @Serializable(with = FlexibleIntSerializer::class)
    val paid_order_count: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val paid_user_count: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val paid_old_user_count: Int = 0
)

@Serializable
data class AfdianDialogsResponse(
    val ec: Int,
    val em: String,
    val data: AfdianDialogsData? = null
)

@Serializable
data class AfdianDialogsData(
    @Serializable(with = FlexibleIntSerializer::class)
    val total_count: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val total_page: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val has_more: Int = 0,
    val list: List<AfdianDialog> = emptyList()
)

@Serializable
data class AfdianDialog(
    @Serializable(with = FlexibleIntSerializer::class)
    val latest_msg_id: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val unread_count: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val total_count: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val status: Int = 0,
    val user: AfdianDialogUser? = null,
    val desc: String = "",
    @Serializable(with = FlexibleIntSerializer::class)
    val send_time: Int = 0
)

@Serializable
data class AfdianDialogUser(
    val user_id: String = "",
    val name: String = "",
    val avatar: String = ""
)

@Serializable
data class AfdianIncomeResponse(
    val ec: Int,
    val em: String,
    val data: AfdianIncomeData? = null
)

@Serializable
data class AfdianIncomeData(
    val monthly_bill: List<AfdianYearlyBill> = emptyList()
)

@Serializable
data class AfdianYearlyBill(
    val year: Int,
    val data: List<AfdianMonthlyBill> = emptyList()
)

@Serializable
data class AfdianMonthlyBill(
    val month: Int,
    val data: AfdianMonthlyBillData
)

@Serializable
data class AfdianMonthlyBillData(
    val total_amount: String = "0.00",
    val creator_amount: String = "0.00",
    @Serializable(with = FlexibleIntSerializer::class)
    val sponsor_count: Int = 0,
    @Serializable(with = FlexibleIntSerializer::class)
    val create_time: Long = 0
)

data class AfdianMonthlyIncome(
    val year: Int,
    val month: Int,
    val totalAmount: Double,
    val creatorAmount: Double,
    val sponsorCount: Int
)

@Serializable
data class AfdianCreatorListResponse(
    val ec: Int,
    val em: String,
    val data: AfdianCreatorListData? = null
)

@Serializable
data class AfdianCreatorListData(
    val list: List<AfdianCreatorItem> = emptyList(),
    @Serializable(with = FlexibleIntSerializer::class)
    val has_more: Int = 0
)

@Serializable
data class AfdianCreatorItem(
    val user_id: String = "",
    val name: String = "",
    val avatar: String = "",
    val url_slug: String = "",
    @Serializable(with = FlexibleIntSerializer::class)
    val is_verified: Int = 0,
    val creator: AfdianCreatorInfo? = null
)

@Serializable
data class AfdianCreatorInfo(
    val doing: String = "",
    val category: AfdianCreatorCategory? = null
)

@Serializable
data class AfdianCreatorCategory(
    val name: String = ""
)

data class AfdianRandomCreator(
    val userId: String,
    val name: String,
    val avatar: String,
    val urlSlug: String,
    val isVerified: Boolean,
    val doing: String,
    val categoryName: String
)

@Serializable
data class AfdianTopSponsorsResponse(
    val ec: Int,
    val em: String,
    val data: AfdianTopSponsorsData? = null
)

@Serializable
data class AfdianTopSponsorsData(
    @Serializable(with = FlexibleIntSerializer::class)
    val has_more: Int = 0,
    val list: List<AfdianSponsorItem> = emptyList()
)

@Serializable
data class AfdianSponsorItem(
    val user_id: String = "",
    val name: String = "",
    val avatar: String = "",
    val url_slug: String = "",
    @Serializable(with = FlexibleIntSerializer::class)
    val is_verified: Int = 0
)

data class AfdianTopSponsor(
    val userId: String,
    val name: String,
    val avatar: String,
    val urlSlug: String,
    val isVerified: Boolean
)

@Serializable
data class AfdianProfileResponse(
    val ec: Int,
    val em: String,
    val data: AfdianProfileData? = null
)

@Serializable
data class AfdianProfileData(
    val user: AfdianProfileUser? = null
)

@Serializable
data class AfdianProfileUser(
    val user_id: String = "",
    val name: String = "",
    val avatar: String = ""
)
