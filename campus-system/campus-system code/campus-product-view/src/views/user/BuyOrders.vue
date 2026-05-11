<template>
    <div>
        <div class="condition">
            <span class="trade">
                <span :style="{
                    color: tradeStatusSelectedItem.name === tradeStatus.name ? 'rgb(51,51,51)' : '',
                    backgroundColor: tradeStatusSelectedItem.name === tradeStatus.name ? 'rgb(255,255,255)' : ''
                }" @click="tradeStatusSelected(tradeStatus)" v-for="(tradeStatus, index) in tradeStatusList"
                    :key="index">{{ tradeStatus.name }}</span>
            </span>
            <el-input size="small" style="width: 166px;" v-model="ordersQueryDto.code" placeholder="Order No." clearable
                @clear="handleFilterClear">
                <el-button slot="append" @click="fetchOrders" icon="el-icon-search"></el-button>
            </el-input>
        </div>
        <div v-if="ordersList.length === 0">
            <el-empty description="No matching orders found"></el-empty>
        </div>
        <div v-else class="item-order" v-for="(orderInfo, index) in ordersList" :key="index">
            <div class="orders-base-info">
                <div class="code">Order No.: {{ orderInfo.code }}</div>
                <div class="code" v-if="orderInfo.tradeStatus">Payment Time: {{ orderInfo.tradeTime }}</div>
                <div class="code" v-if="orderInfo.isRefundConfirm">Refund Time: {{ orderInfo.refundTime }}</div>
            </div>
            <div class="info">
                <div>
                    <img :src="orderInfo.cover" alt="" srcset="">
                </div>
                <div>
                    <div>
                        <span class="title">{{ orderInfo.productTitle }}</span>
                        <span class="number">x{{ orderInfo.buyNumber }}</span>
                    </div>
                    <div class="detail">
                        Notes: {{ orderInfo.detail }}
                    </div>
                    <div>
                        <span class="symbol">$</span>
                        <span class="price">{{ totalPrice(orderInfo) }}</span>
                    </div>
                    <div class="orders-base-info">Created At: {{ orderInfo.createTime }}</div>
                    <div style="margin-block: 10px;">
                        <span v-if="orderInfo.refundStatus" style="color: rgb(0, 121, 186);">
                            {{ orderInfo.isRefundConfirm ? 'Refund has been returned to the original account' : 'Refund request pending seller confirmation...' }}
                        </span>
                        <span v-else>
                            <span @click="placeAnOrder(orderInfo)" v-if="!orderInfo.tradeStatus"
                                class="edit-button">Place Order</span>
                            <span @click="refund(orderInfo)" v-else class="edit-button">Request Refund</span>
                            <span style="margin-left: 5px;" @click="del(orderInfo)" v-if="orderInfo.tradeStatus"
                                class="channel-button">Delete</span>
                        </span>

                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
export default {
    name: 'BuyOrders',
    data() {
        return {
            ordersList: [],
            ordersQueryDto: {},
            tradeStatusSelectedItem: {},
            tradeStatusList: [{ tradeStatus: null, name: 'All' }, { tradeStatus: true, name: 'Paid' }, { tradeStatus: false, name: 'Unpaid' }]
        }
    },
    created() {
        this.fetchOrders();
        this.tradeStatusSelected(this.tradeStatusList[0]);
    },
    methods: {
        tradeStatusSelected(tradeStatusItem) {
            this.tradeStatusSelectedItem = tradeStatusItem;
            this.ordersQueryDto.tradeStatus = tradeStatusItem.tradeStatus;
            this.fetchOrders();
        },
        handleFilterClear() {
            this.ordersQueryDto.code = '';
            this.fetchOrders();
        },
        async del(orderInfo) {
            const confirmed = await this.$swalConfirm({
                title: 'Delete Order',
                text: `This action cannot be undone. Do you want to continue?`,
                icon: 'warning',
            });
            if (confirmed) {
                try {
                    let ids = [orderInfo.id];
                    const response = await this.$axios.post(`/orders/batchDelete`, ids);
                    if (response.data.code === 200) {
                        this.$notify({
                            duration: 1000,
                            title: 'Delete',
                            message: 'Deleted successfully',
                            type: 'success'
                        });
                        this.fetchOrders();
                        return;
                    }
                } catch (error) {
                    this.$message.error("Order deletion error:", error);
                    console.error(`Order deletion error:`, error);
                }
            }
        },
        /**
         * 计算总金额
         */
        totalPrice(orderInfo) {
            const totalPrice = orderInfo.buyNumber * orderInfo.buyPrice;
            // 保留两位小数点
            return parseFloat(totalPrice).toFixed(2);
        },
        refund(orders) {
            this.$axios.post(`/product/refund/${orders.id}`).then(res => {
                const { data } = res; // 解构
                if (data.code === 200) {
                    this.$notify({
                        duration: 1000,
                        title: 'Refund',
                        message: 'Refund successful',
                        type: 'success'
                    });
                    this.fetchOrders();
                }
            }).catch(error => {
                console.log("Refund error:", error);
            })
        },
        placeAnOrder(orders) {
            this.$axios.post(`/product/placeAnOrder/${orders.id}`).then(res => {
                const { data } = res; // 解构
                if (data.code === 200) {
                    this.$notify({
                        duration: 1000,
                        title: 'Order',
                        message: 'Order placed successfully',
                        type: 'success'
                    });
                    this.fetchOrders();
                }
            }).catch(error => {
                console.log("Order error:", error);
            })
        },
        coverParse(coverList) {
            if (coverList.productCover === null) {
                return;
            }
            return coverList.productCover.split(',')[0];
        },
        /**
         * 购物订单
         */
        fetchOrders() {
            this.$axios.post('/orders/queryUser', this.ordersQueryDto).then(res => {
                const { data } = res; // 解构
                if (data.code === 200) {
                    this.ordersList = data.data.map(order => {
                        return {
                            id: order.id,
                            code: order.code,
                            productTitle: order.productTitle,
                            detail: order.detail,
                            buyPrice: order.buyPrice,
                            buyNumber: order.buyNumber,
                            tradeStatus: order.tradeStatus,
                            tradeTime: order.tradeTime,
                            refundStatus: order.refundStatus,
                            refundTime: order.refundTime,
                            isRefundConfirm: order.isRefundConfirm,
                            createTime: order.createTime,
                            tradeTime: order.tradeTime,
                            refundTime: order.refundTime,
                            cover: this.coverParse(order)
                        }
                    });
                }
            }).catch(error => {
                console.log("Order data query error:", error);
            })
        },
    }
};
</script>
<style scoped lang="scss">
.condition {
    display: flex;
    justify-content: right;

    .trade {
        display: inline-block;
        font-size: 12px;
        background-color: rgb(246, 246, 246);
        line-height: 24px;
        padding-inline: 10px;
        padding-block: 4px;
        margin-right: 5px;
        border-radius: 5px;
        cursor: pointer;

        span {
            display: inline-block;
            padding-inline: 10px;
            border-radius: 5px;
        }
    }
}

.item-order {
    font-size: 14px;
    margin-block: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid rgb(246, 246, 246);

    .orders-base-info{
        font-size: 14px;
        margin-block: 6px;
        color: rgb(117, 117, 117);
        .code{
            margin-block: 4px;
        }
    }

    .info {
        .detail {
            margin-block: 4px;
            font-size: 14px;
        }

        img {
            width: 140px;
        }

        display: flex;
        justify-content: left;
        gap: 20px;

        .symbol {
            font-size: 12px;
            margin-right: 4px;
            color: rgb(255, 79, 36);
        }

        .price {
            font-size: 18px;
            font-weight: 800;
            color: rgb(255, 79, 36);
        }

        .title {
            font-size: 20px;
            font-weight: 800;
        }

        .number {
            margin-left: 6px;
        }
    }
}
</style>