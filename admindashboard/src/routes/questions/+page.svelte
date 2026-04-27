<script lang="ts">
	import { getQuestions, createQuestion, deleteQuestion, type Question } from '$lib/api';

	let items = $state<Question[]>([]);
	let total = $state(0);
	let loading = $state(true);
	let error = $state('');
	let showForm = $state(false);
	let filterClass = $state<number | ''>('');
	let filterSubject = $state('');

	const SUBJECTS = ['Mathematics', 'Science', 'English', 'Nepali', 'Social Studies', 'Physics', 'Chemistry', 'Biology', 'Computer', 'Health & PE'];
	const CLASS_LEVELS = [8, 10, 12];
	const DIFFICULTIES = ['easy', 'medium', 'hard'];

	let formId = $state('');
	let formClassLevel = $state(10);
	let formSubject = $state('Mathematics');
	let formTopic = $state('');
	let formPrompt = $state('');
	let formOptions = $state('Option A|Option B|Option C|Option D');
	let formCorrectIndex = $state(0);
	let formExplanation = $state('');
	let formDifficulty = $state('medium');

	async function load() {
		loading = true;
		error = '';
		try {
			const res = await getQuestions(filterClass ? Number(filterClass) : undefined, filterSubject || undefined, 200, 0);
			items = res.items;
			total = res.total;
		} catch (e: any) {
			error = e.message;
		} finally {
			loading = false;
		}
	}

	function openCreate() {
		formId = `q${Date.now()}`;
		formClassLevel = 10;
		formSubject = 'Mathematics';
		formTopic = '';
		formPrompt = '';
		formOptions = 'Option A|Option B|Option C|Option D';
		formCorrectIndex = 0;
		formExplanation = '';
		formDifficulty = 'medium';
		showForm = true;
	}

	async function handleSave() {
		error = '';
		try {
			await createQuestion({
				id: formId,
				classLevel: formClassLevel,
				subject: formSubject,
				topic: formTopic || 'general',
				prompt: formPrompt,
				options: formOptions.split('|').map(o => o.trim()),
				correctIndex: formCorrectIndex,
				explanation: formExplanation || null,
				source: 'admin',
				language: 'en',
				difficulty: formDifficulty,
			});
			showForm = false;
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	async function handleDelete(id: string) {
		if (!confirm(`Delete question "${id}"?`)) return;
		try {
			await deleteQuestion(id);
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	load();
</script>

<div class="page-header">
	<h2>Questions</h2>
	<p>Manage MCQ questions for quizzes</p>
</div>

<div style="display:flex; gap:12px; align-items:center; margin-bottom:20px; flex-wrap:wrap;">
	<select class="form-input" style="width:auto;padding:6px 12px;font-size:13px" bind:value={filterClass} onchange={load}>
		<option value="">All Classes</option>
		{#each CLASS_LEVELS as c}<option value={c}>Class {c}</option>{/each}
	</select>
	<select class="form-input" style="width:auto;padding:6px 12px;font-size:13px" bind:value={filterSubject} onchange={load}>
		<option value="">All Subjects</option>
		{#each SUBJECTS as s}<option value={s}>{s}</option>{/each}
	</select>
	<span style="color:var(--text-secondary); font-size:13px; margin-left:auto;">{total} questions</span>
	<button class="btn btn-primary" onclick={openCreate}>+ Add Question</button>
</div>

{#if error}
	<div class="error-msg">{error}</div>
{/if}

{#if showForm}
	<div class="modal-overlay" onclick={() => showForm = false}>
		<div class="modal" onclick={(e) => e.stopPropagation()}>
			<h3>New Question</h3>
			{#if error}<div class="error-msg">{error}</div>{/if}
			<div class="form-grid">
				<div class="form-group">
					<label>Class Level</label>
					<select class="form-input" bind:value={formClassLevel}>
						{#each CLASS_LEVELS as c}<option value={c}>Class {c}</option>{/each}
					</select>
				</div>
				<div class="form-group">
					<label>Subject</label>
					<select class="form-input" bind:value={formSubject}>
						{#each SUBJECTS as s}<option value={s}>{s}</option>{/each}
					</select>
				</div>
				<div class="form-group">
					<label>Topic</label>
					<input class="form-input" bind:value={formTopic} placeholder="Algebra, Optics, etc." />
				</div>
				<div class="form-group">
					<label>Difficulty</label>
					<select class="form-input" bind:value={formDifficulty}>
						{#each DIFFICULTIES as d}<option value={d}>{d}</option>{/each}
					</select>
				</div>
				<div class="form-group full">
					<label>Question Prompt</label>
					<textarea class="form-input" bind:value={formPrompt} placeholder="What is the value of...?" rows="3"></textarea>
				</div>
				<div class="form-group full">
					<label>Options (pipe-separated)</label>
					<input class="form-input" bind:value={formOptions} placeholder="Option A|Option B|Option C|Option D" />
				</div>
				<div class="form-group">
					<label>Correct Option Index (0-based)</label>
					<input class="form-input" type="number" min="0" max="5" bind:value={formCorrectIndex} />
				</div>
				<div class="form-group full">
					<label>Explanation (optional)</label>
					<textarea class="form-input" bind:value={formExplanation} placeholder="Explain why the answer is correct..." rows="2"></textarea>
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
		<div class="icon">?</div>
		<h3>No questions found</h3>
		<p>Add MCQ questions for classes 8, 10, and 12</p>
	</div>
{:else}
	<div class="table-wrap animate-in">
		<table>
			<thead>
				<tr>
					<th>Class</th>
					<th>Subject</th>
					<th>Topic</th>
					<th>Prompt</th>
					<th>Difficulty</th>
					<th>Source</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				{#each items as item}
					<tr>
						<td>{item.classLevel}</td>
						<td>{item.subject}</td>
						<td>{item.topic}</td>
						<td style="max-width:300px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:var(--text)">{item.prompt}</td>
						<td>
							<span class="badge" class:badge-green={item.difficulty === 'easy'} class:badge-yellow={item.difficulty === 'medium'} class:badge-red={item.difficulty === 'hard'}>
								{item.difficulty}
							</span>
						</td>
						<td><span class="badge badge-accent">{item.source}</span></td>
						<td>
							<button class="btn btn-danger btn-sm" onclick={() => handleDelete(item.id)}>Delete</button>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
{/if}