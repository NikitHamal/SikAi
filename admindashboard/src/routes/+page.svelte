<script lang="ts">
	import { goto } from '$app/navigation';
	import { isLoggedIn } from '$lib/api';

	if (isLoggedIn()) {
		goto('/dashboard');
	} else {
		goto('/login');
	}
</script>