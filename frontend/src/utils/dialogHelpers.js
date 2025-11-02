/**
 * 다이얼로그 헬퍼 함수들
 * 컴포넌트에서 직접 다이얼로그를 관리하기 위한 유틸리티
 */

/**
 * 다이얼로그 상태 관리 헬퍼
 * @returns {Object} 다이얼로그 상태 및 제어 함수
 */
export function useDialog() {
  return {
    confirmDialog: {
      isVisible: false,
      title: '확인',
      message: '',
      confirmText: '확인',
      cancelText: '취소',
      confirmVariant: 'danger',
      resolve: null,
      reject: null
    },
    alertDialog: {
      isVisible: false,
      title: '알림',
      message: '',
      buttonText: '확인',
      buttonVariant: 'primary',
      resolve: null
    }
  }
}

/**
 * ConfirmDialog 사용 예시를 위한 템플릿 코드
 * 각 컴포넌트에서 다음과 같이 사용:
 * 
 * const dialogState = ref(useDialog())
 * 
 * const showConfirm = (message, title = '확인') => {
 *   return new Promise((resolve, reject) => {
 *     dialogState.value.confirmDialog = {
 *       isVisible: true,
 *       title,
 *       message,
 *       confirmText: '확인',
 *       cancelText: '취소',
 *       confirmVariant: 'danger',
 *       resolve,
 *       reject
 *     }
 *   })
 * }
 * 
 * const handleConfirm = () => {
 *   if (dialogState.value.confirmDialog.resolve) {
 *     dialogState.value.confirmDialog.resolve(true)
 *   }
 *   dialogState.value.confirmDialog.isVisible = false
 * }
 * 
 * const handleCancel = () => {
 *   if (dialogState.value.confirmDialog.reject) {
 *     dialogState.value.confirmDialog.reject(false)
 *   }
 *   dialogState.value.confirmDialog.isVisible = false
 * }
 * 
 * const showAlert = (message, title = '알림', variant = 'primary') => {
 *   return new Promise((resolve) => {
 *     dialogState.value.alertDialog = {
 *       isVisible: true,
 *       title,
 *       message,
 *       buttonText: '확인',
 *       buttonVariant: variant,
 *       resolve
 *     }
 *   })
 * }
 * 
 * const handleAlertClose = () => {
 *   if (dialogState.value.alertDialog.resolve) {
 *     dialogState.value.alertDialog.resolve()
 *   }
 *   dialogState.value.alertDialog.isVisible = false
 * }
 */

