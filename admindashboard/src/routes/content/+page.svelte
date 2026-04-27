<script lang="ts">
	import { getManifest, upsertManifest, deleteManifest, type ManifestItem } from '$lib/api';

	let items = $state<ManifestItem[]>([]);
	let total = $state(0);
	let loading = $state(true);
	let error = $state('');
	let showForm = $state(false);
	let editingItem = $state<ManifestItem | null>(null);

	let formId = $state('');
	let formTitle = $state('');
	let formType = $state('past_paper');
	let formClassLevel = $state(10);
	let formSubject = $state('Mathematics');
	let formYear = $state<number | ''>('');
	let formFileUrl = $state('');
	let formLanguage = $state('en');
	let formTags = $state('');

	const TYPES = ['past_paper', 'syllabus', 'textbook', 'notes', 'mcq_pack', 'model_question', 'solution'];
	const SUBJECTS = ['Mathematics', 'Science', 'English', 'Nepali', 'Social Studies', 'Physics', 'Chemistry', 'Biology', 'Computer', 'All'];
	const CLASS_LEVELS = [8, 10, 12];

	async function load() {
		loading = true;
		error = '';
		try {
			const res = await getManifest(200, 0);
			items = res.items;
			total = res.total;
		} catch (e: any) {
			error = e.message;
		} finally {
			loading = false;
		}
	}

	function openCreate() {
		editingItem = null;
		formId = '';
		formTitle = '';
		formType = 'past_paper';
		formClassLevel = 10;
		formSubject = 'Mathematics';
		formYear = '';
		formFileUrl = '';
		formLanguage = 'en';
		formTags = '';
		showForm = true;
	}

	function openEdit(item: ManifestItem) {
		editingItem = item;
		formId = item.id;
		formTitle = item.title;
		formType = item.type;
		formClassLevel = item.classLevel;
		formSubject = item.subject;
		formYear = item.year ?? '';
		formFileUrl = item.fileUrl ?? '';
		formLanguage = item.language;
		formTags = item.tags.join(', ');
		showForm = true;
	}

	async function handleSave() {
		error = '';
		try {
			const item: ManifestItem = {
				id: formId,
				title: formTitle,
				type: formType,
				classLevel: formClassLevel,
				subject: formSubject,
				year: formYear ? Number(formYear) : null,
				fileUrl: formFileUrl || null,
				sizeBytes: 0,
				checksumSha256: null,
				version: 1,
				updatedAt: Date.now(),
				language: formLanguage,
				tags: formTags.split(',').map(t => t.trim()).filter(Boolean),
			};
			await upsertManifest([item]);
			showForm = false;
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	async function handleDelete(id: string) {
		if (!confirm(`Delete "${id}"?`)) return;
		try {
			await deleteManifest(id);
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	load();
</script>

<div class="page-header">
	<h2>Content Manifest</h2>
	<p>Manage downloadable content — past papers, syllabi, textbooks. Host files on GitHub Releases or any CDN and paste the URL here.</p>
</div>

<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
	<span style="color:var(--text-secondary); font-size:13px;">{total} items</span>
	<button class="btn btn-primary" onclick={openCreate}>+ Add Content</button>
</div>

{#if error}
	<div class="error-msg">{error}</div>
{/if}

{#if showForm}
	<!-- svelte-ignore a11y_click_events_have_key_events a11y_no_static_element_interactions -->
	<div class="modal-overlay" onclick={() => showForm = false}>
		<!-- svelte-ignore a11y_click_events_have_key_events a11y_no_static_element_interactions -->
		<div class="modal" onclick={(e) => e.stopPropagation()}>
			<h3>{editingItem ? 'Edit Content' : 'Add Content'}</h3>
			{#if error}<div class="error-msg">{error}</div>{/if}
			<div class="form-grid">
				<div class="form-group">
					<label for="cm-id">ID</label>
					<input id="cm-id" class="form-input" bind:value={formId} placeholder="see-2080-math" disabled={!!editingItem} />
				</div>
				<div class="form-group">
					<label for="cm-type">Type</label>
					<select id="cm-type" class="form-input" bind:value={formType}>
						{#each TYPES as t}<option value={t}>{t.replace(/_/g, ' ')}</option>{/each}
					</select>
				</div>
				<div class="form-group full">
					<label for="cm-title">Title</label>
					<input id="cm-title" class="form-input" bind:value={formTitle} placeholder="SEE 2080 Mathematics — Full Paper" />
				</div>
				<div class="form-group">
					<label for="cm-class">Class Level</label>
					<select id="cm-class" class="form-input" bind:value={formClassLevel}>
						{#each CLASS_LEVELS as c}<option value={c}>Class {c}</option>{/each}
					</select>
				</div>
				<div class="form-group">
					<label for="cm-subject">Subject</label>
					<select id="cm-subject" class="form-input" bind:value={formSubject}>
						{#each SUBJECTS as s}<option value={s}>{s}</option>{/each}
					</select>
				</div>
				<div class="form-group">
					<label for="cm-year">Year (optional)</label>
					<input id="cm-year" class="form-input" type="number" bind:value={formYear} placeholder="2080" />
				</div>
				<div class="form-group">
					<label for="cm-lang">Language</label>
					<select id="cm-lang" class="form-input" bind:value={formLanguage}>
						<option value="en">English</option>
						<option value="ne">Nepali</option>
					</select>
				</div>
				<div class="form-group full">
					<label for="cm-url">Download URL (GitHub Releases, CDN, etc.)</label>
					<input id="cm-url" class="form-input" bind:value={formFileUrl} placeholder="https://github.com/OWNER/REPO/releases/download/content/see-2080-math.pdf" />
				</div>
				<div class="form-group full">
					<label for="cm-tags">Tags (comma-separated)</label>
					<input id="cm-tags" class="form-input" bind:value={formTags} placeholder="see, 2080, mathematics" />
				</div>
			</div>
			<div class="modal-actions">
				<button class="btn btn-secondary" onclick={() => showForm = false}>Cancel</button>
				<button class="btn btn-primary" onclick={handleSave}>Save</button>
			</div>
		</div>
	</div>
{/if}

{#if loading}
	<p style="color:var(--text-tertiary)">Loading...</p>
{:else if items.length === 0}
	<div class="empty-state">
		<div class="icon">▤</div>
		<h3>No content yet</h3>
		<p>Add past papers, syllabi, and textbooks</p>
	</div>
{:else}
	<div class="table-wrap animate-in">
		<table>
			<thead>
				<tr>
					<th>ID</th>
					<th>Title</th>
					<th>Type</th>
					<th>Class</th>
					<th>Subject</th>
					<th>Year</th>
					<th>URL</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				{#each items as item}
					<tr>
						<td style="font-family:monospace; font-size:12px; color:var(--accent)">{item.id}</td>
						<td style="color:var(--text)">{item.title}</td>
						<td><span class="badge badge-accent">{item.type.replace(/_/g, ' ')}</span></td>
						<td>{item.classLevel}</td>
						<td>{item.subject}</td>
						<td>{item.year ?? '—'}</td>
						<td style="max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; font-size:12px">
							{item.fileUrl ? '✓ ' + item.fileUrl.split('/').pop() : '—'}
						</td>
						<td>
							<button class="btn btn-secondary btn-sm" onclick={() => openEdit(item)}>Edit</button>
							<button class="btn btn-danger btn-sm" onclick={() => handleDelete(item.id)}>Delete</button>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
{/if}