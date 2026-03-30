<template>
    <div class="product-list">
        <el-row v-if="productList.length === 0">
            <el-empty description="No product information available"></el-empty>
        </el-row>
        <el-row v-else>
            <el-col :span="6" v-for="(product, index) in productList" :key="index">
                <div class="item-product">
                    <div class="cover">
                        <img :src="coverListParse(product)" alt="" srcset="">
                    </div>
                    <div style="display: flex;justify-content: left;gap: 4px;align-items: center;">
                        <span class="bargain-hover">{{ product.isBargain ? 'Negotiable' : 'Non-negotiable' }}</span>
                        <span class="title" @click="route(product)">
                            {{ product.name }}
                        </span>
                    </div>
                    <div style="padding-block: 15px;">
                        <span class="decimel-symbol">¥</span>
                        <span class="price">{{ product.price }}</span>
                        <span class="love">4 people want this</span>
                    </div>
                    <div>
                        <span @click="handleEdit(product)" class="edit-button">Edit</span>
                        <span @click="handleDelete(product)" class="channel-button">Delete</span>
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
</template>
<script>
import { setProductInfo } from "@/utils/storage"
export default {
    name: 'MyProduct',
    data() {
        return {
            productList: []
        };
    },
    created() {
        this.fetchProduct();
    },
    methods: {
        route(product) {
            // 跳转商品详情
            this.$router.push('/product-detail?productId=' + product.id);
        },
        /**
         * 商品编辑
         * @param {*} product 待处理的商品信息
         */
        handleEdit(product) {
            // 先将待操作商品信息存起来
            setProductInfo(product);
            this.$router.push('/edit-product');
        },
        /**
         * 商品删除
         * @param {*} product 待处理的商品信息
         */
        async handleDelete(product) {
            const confirmed = await this.$swalConfirm({
                title: `Delete product [${product.name}]`,
                text: `This action cannot be undone. Continue?`,
                icon: 'warning',
            });
            if (confirmed) {
                try {
                    let ids = [product.id]
                    const response = await this.$axios.post(`/product/batchDelete`, ids);
                    if (response.data.code === 200) {
                        this.$notify({
                            duration: 1000,
                            title: 'Delete Info',
                            message: 'Deleted successfully',
                            type: 'success'
                        });
                        this.fetchProduct();
                        return;
                    }
                } catch (error) {
                    this.$notify({
                        duration: 2000,
                        title: 'Delete Info',
                        message: error,
                        type: 'error'
                    });
                    console.error(`Product deletion error:`, error);
                }
            }

        },
        /**
         * 商品封面图处理
         * 从字符串转成可用数组
         * @param {*} product 待处理商品信息
         */
        coverListParse(product) {
            if (product.coverList === null) {
                return;
            }
            const newCoverList = product.coverList.split(',');
            return newCoverList[0];
        },
        /**
         * 查询用户自己发布的商品信息
         */
        fetchProduct() {
            this.$axios.post('/product/queryUser', {}).then(res => {
                const { data } = res; // 解构
                if (data.code === 200) {
                    this.productList = data.data;
                }
            }).catch(error => {
                console.log("Product query error:", error);
            })
        },
    }
};
</script>
<style scoped lang="scss">
.product-list {
    padding-block: 20px;

    .item-product {
        padding: 10px 10px 16px 10px;
        box-sizing: border-box;
        border-radius: 15px;
        transition: all .5s;
        cursor: pointer;

        .cover {
            img {
                width: 100%;
                height: 240px;
                border-radius: 10px;
            }
        }

        .bargain-hover {
            font-size: 12px;
            font-weight: 800;
            background-color: rgb(255, 230, 15);
            color: rgb(51, 51, 51);
            border-radius: 2px;
            padding: 2px 6px;
        }

        .title {
            font-size: 20px;
            color: #1f1f1f;
        }

        .decimel-symbol {
            font-size: 14px;
            color: #ff4f24;
            font-weight: 800;
        }

        .price {
            font-size: 24px;
            color: #ff4f24;
            font-weight: 800;
            margin-right: 6px;
        }

        .love {
            font-size: 14px;
            color: #999;
        }

    }

    .item-product:hover {
        box-shadow: 1px 4px 6px rgba(0, 0, 0, 0.1), 0 1px 3px rgba(0, 0, 0, 0.06);
    }
}
</style>