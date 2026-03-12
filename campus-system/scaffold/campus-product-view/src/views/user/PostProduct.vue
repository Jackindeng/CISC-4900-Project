<template>
    <div class="post-product">
        <div>
            <div>
                <p>Product Name</p>
                <input class="dialog-input" v-model="product.name" placeholder="Product Name" />
            </div>
            <div>
                <p>Condition</p>
                <el-input-number v-model="oldLevel" @change="handleChange" :min="1" :max="10"
                    label="Condition"></el-input-number>
            </div>
            <div>
                <p>Price</p>
                <input class="dialog-input" v-model="product.price" placeholder="Price" />
            </div>
            <div>
                <p>Negotiable</p>
                <el-switch v-model="product.isBargain" active-text="Negotiable" inactive-text="Not Negotiable">
                </el-switch>
            </div>
            <div>
                <span class="post" @click="postProduct">Post Product</span>
            </div>
        </div>
        <div>
            <div>
                <p>Product Images</p>
                <el-upload action="http://localhost:21090/api/campus-product-sys/v1.0/file/upload"
                    list-type="picture-card" :on-success="handlePictureCardSuccess"
                    :on-preview="handlePictureCardPreview" :on-remove="handleRemove">
                    <i class="el-icon-plus"></i>
                </el-upload>
                <el-dialog :visible.sync="dialogVisible">
                    <img width="100%" :src="dialogImageUrl" alt="">
                </el-dialog>
            </div>
            <div>
                <p>Category</p>
                <span :style="{
                    backgroundColor: categorySelected.id === category.id ? 'rgb(76, 147, 209)' : '',
                    color: categorySelected.id === category.id ? 'rgb(244,244,244)' : ''
                }" class="category-item" @click="categoryClick(category)" v-for="(category, index) in categoryList"
                    :key="index">
                    {{ category.name }}
                </span>
            </div>
            <div>
                <p>Stock</p>
                <el-input-number v-model="inventory" @change="handleInventoryChange" :min="1" :max="10000"
                    label="Stock"></el-input-number>
            </div>
            <div>
                <Editor height="calc(100vh - 400px)" :receiveContent="product.detail" @on-receive="onReceive" />
            </div>
        </div>
    </div>
</template>

<script>
import Editor from "@/components/Editor"

export default {
    components: { Editor },
    name: 'PostProduct',
    data() {
        return {
            oldLevel: 9, // 默认九成新
            inventory: 1, // 默认库存
            product: {
                oldLevel: 9,
                inventory: 1
            },
            categorySelected: {}, // 当前选中的商品类别
            dialogImageUrl: '',
            dialogVisible: false,
            coverList: [],
            categoryList: []
        };
    },
    created() {
        this.fetchCategoryList();
    },
    methods: {
        /**
         * 发布商品
         */
        postProduct() {
            if (this.product.name === undefined || this.product.name === '') {
                this.$notify.info({
                    duration: 1000,
                    title: 'Input Reminder',
                    message: 'Product title cannot be empty'
                });
                return;
            }
            if (this.product.price === undefined || this.product.price === '') {
                this.$notify.info({
                    duration: 1000,
                    title: 'Input Reminder',
                    message: 'Product price cannot be empty'
                });
                return;
            }
            if (this.coverList.length === 0) {
                this.$notify.info({
                    duration: 1000,
                    title: 'Upload Reminder',
                    message: 'Please upload a cover image'
                });
                return;
            }

            this.product.oldLevel = this.product.oldLevel != null ? this.product.oldLevel : this.oldLevel;
            this.product.inventory = this.product.inventory != null ? this.product.inventory : this.inventory;
            this.product.coverList = this.coverList.join(',');

            this.$axios.post('/product/save', this.product).then(res => {
                const { data } = res;
                if (data.code === 200) {
                    this.$notify.success({
                        title: 'Post Product',
                        message: data.msg
                    });
                    // 后续清除之后，跳转我的商品页面
                    // this.product = {};
                }
            }).catch(error => {
                this.$notify.error({
                    title: 'Post Product',
                    message: error
                });
                console.log("Product creation error:", error);
            });
        },
        /**
         * 富文本编辑器回调内容
         * @param {*} detail 
         */
        onReceive(detail) {
            this.product.detail = detail;
        },
        /**
         * 商品分类选中事件
         * @param {*} category 商品分类
         */
        categoryClick(category) {
            this.categorySelected = category;
            this.product.categoryId = category.id;
        },
        /**
         * 查询商品类别
         */
        fetchCategoryList() {
            this.$axios.post('/category/query', {}).then(res => {
                const { data } = res;
                if (data.code === 200) {
                    this.categoryList = data.data;
                    // 默认选中第一项
                    this.categoryClick(this.categoryList[0]);
                }
            }).catch(error => {
                console.log("Category query error:", error);
            });
        },
        /**
         * 新旧程度选择事件
         */
        handleChange() {
            console.log("Condition:", this.oldLevel);
            this.product.oldLevel = this.oldLevel;
        },
        /**
         * 库存设置事件
         */
        handleInventoryChange() {
            console.log("Stock:", this.inventory);
            this.product.inventory = this.inventory;
        },
        /**
         * 封面上传成功响应事件
         * @param {*} file 
         * @param {*} fileList 
         */
        handlePictureCardSuccess(file, fileList) {
            this.coverList.push(file.data);
        },
        handleRemove(file, fileList) {
            this.coverList = fileList.map(function (item) {
                return item.data || item.url || (item.response && item.response.data);
            }).filter(function (item) {
                return !!item;
            });
            console.log(file, fileList);
        },
        handlePictureCardPreview(file) {
            this.dialogImageUrl = file.url;
            this.dialogVisible = true;
        }
    }
};
</script>

<style scoped lang="scss">
.post {
    display: inline-block;
    background-color: rgb(51, 51, 51);
    margin-block: 30px;
    width: 100%;
    text-align: center;
    padding: 15px;
    box-sizing: border-box;
    color: rgb(245, 245, 245);
    border-radius: 5px;
    font-size: 14px;
    cursor: pointer;
}

.post:hover {
    background-color: rgb(31, 31, 31);
}

.category-item {
    display: inline-block;
    padding: 6px 20px;
    background-color: rgb(246, 246, 246);
    border-radius: 5px;
    font-size: 14px;
    cursor: pointer;
    margin: 4px;
}

.category-item:hover {
    background-color: rgb(241, 241, 241);
}

.post-product {
    display: flex;
    justify-content: left;
    gap: 30px;
}
</style>