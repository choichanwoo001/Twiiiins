import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'

export function useAppScroll(route, isMobile) {
    const showScrollUp = ref(false)
    const scrollY = ref(0)
    const isBottomReached = ref(false)
    let scrollContainer = null
    let scrollListener = null
    const resizeObserver = ref(null)

    const updateScrollUpVisibility = () => {
        const { scrollHeight, clientHeight } = document.documentElement
        const isScrollable = scrollHeight > clientHeight + 1
        const hasScrolled = window.scrollY > 100
        showScrollUp.value = isScrollable && hasScrolled
    }

    const updateScrollY = () => {
        let currentScrollTop = 0
        let currentClientHeight = 0
        let currentScrollHeight = 0

        // About 페이지이면서 데스크탑일 때만 내부 스크롤 컨테이너 사용
        const isAboutPage = route.path === '/about'

        if (isAboutPage && !isMobile.value && scrollContainer) {
            currentScrollTop = scrollContainer.scrollTop
            currentClientHeight = scrollContainer.clientHeight
            currentScrollHeight = scrollContainer.scrollHeight

            scrollY.value = currentScrollTop
        } else {
            currentScrollTop = window.scrollY
            currentClientHeight = window.innerHeight
            currentScrollHeight = document.documentElement.scrollHeight

            scrollY.value = currentScrollTop
        }

        // 바닥 도달 체크 (여유값 150px)
        const threshold = 150
        if (currentScrollHeight <= currentClientHeight) {
            isBottomReached.value = true
        } else {
            isBottomReached.value = (currentScrollTop + currentClientHeight) >= (currentScrollHeight - threshold)
        }
    }

    const setupAboutPageScroll = () => {
        if (scrollListener && scrollContainer) {
            scrollContainer.removeEventListener('scroll', scrollListener)
        }

        scrollContainer = null
        scrollListener = null

        if (route.path === '/about') {
            nextTick(() => {
                if (!isMobile.value) {
                    const aboutElement = document.querySelector('.about')
                    if (aboutElement) {
                        scrollContainer = aboutElement
                        scrollListener = () => {
                            updateScrollY()
                        }
                        scrollContainer.addEventListener('scroll', scrollListener, { passive: true })
                    }
                }
                updateScrollY()
            })
        } else {
            updateScrollY()
        }
    }

    const handleWindowScroll = () => {
        updateScrollUpVisibility()
        // About 페이지 데스크탑 모드가 아닐 때만 window 스크롤 업데이트
        if (!(route.path === '/about' && !isMobile.value)) {
            updateScrollY()
        }
    }

    const scrollToTop = () => {
        if (route.path === '/about' && !isMobile.value && scrollContainer) {
            scrollContainer.scrollTo({
                top: 0,
                behavior: 'smooth'
            })
        } else {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            })
        }
    }

    // Setup listeners
    onMounted(() => {
        window.addEventListener('scroll', handleWindowScroll, { passive: true })

        resizeObserver.value = new ResizeObserver(() => {
            updateScrollY()
            updateScrollUpVisibility()
        })
        resizeObserver.value.observe(document.body)

        setupAboutPageScroll()
    })

    onUnmounted(() => {
        window.removeEventListener('scroll', handleWindowScroll)
        if (resizeObserver.value) {
            resizeObserver.value.disconnect()
        }
        if (scrollListener && scrollContainer) {
            scrollContainer.removeEventListener('scroll', scrollListener)
        }
    })

    // Watchers
    watch(() => route.path, () => {
        setupAboutPageScroll()
        isBottomReached.value = false
        nextTick(() => {
            updateScrollY()
        })
    }, { immediate: true })

    // 모바일 상태 변경 시 재설정 (App.vue에서 호출 필요)
    watch(isMobile, () => {
        setupAboutPageScroll()
        updateScrollUpVisibility()
        updateScrollY()
    })

    return {
        showScrollUp,
        scrollY,
        isBottomReached,
        scrollToTop,
        setupAboutPageScroll, // 외부에서 호출 가능하게 노출 (resize 시 사용)
        updateScrollUpVisibility,
        updateScrollY
    }
}
