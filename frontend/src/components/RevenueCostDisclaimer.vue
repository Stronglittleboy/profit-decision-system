<script setup lang="ts">
import { ref } from 'vue'
import { CALIBER_BLOCK_FULL } from '@/constants/productCopy'

const active = ref(['caliber'])
</script>

<template>
  <el-collapse v-model="active" class="caliber-collapse">
    <el-collapse-item name="caliber">
      <template #title>
        <span class="caliber-title">经营数字与应收应付口径说明</span>
      </template>
      <p class="caliber-body">{{ CALIBER_BLOCK_FULL }}</p>
    </el-collapse-item>
  </el-collapse>
</template>

<style scoped>
.caliber-collapse {
  margin-bottom: 18px;
  border-radius: 8px;
  overflow: hidden;
}
.caliber-title {
  font-weight: 600;
  font-size: 14px;
}
.caliber-body {
  margin: 0;
  line-height: 1.65;
  color: var(--text);
  font-size: 13px;
}
</style>
