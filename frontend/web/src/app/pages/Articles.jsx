import { useState, useRef } from "react";
import { Plus, Search, Edit, Trash2, Eye, Image as ImageIcon, Link as LinkIcon } from "lucide-react";
import { StatusBadge } from "../components/admin/StatusBadge";
import { ActionButton } from "../components/admin/ActionButton";
import { FilterBar } from "../components/admin/FilterBar";
import { toast } from "sonner";
import { ChevronDown } from "lucide-react";
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

const mockArticles = [
  {
    id: 1,
    title: "Understanding Your Menstrual Cycle",
    category: "Symptoms",
    status: "published",
    author: "MoonSync Team",
    publishDate: "2024-02-08",
    views: 1240,
  },
  {
    id: 2,
    title: "Natural Remedies for Period Pain",
    category: "Lifestyle",
    status: "published",
    author: "Dr. Sarah Johnson",
    publishDate: "2024-02-10",
    views: 856,
  },
  {
    id: 3,
    title: "Nutrition and Your Cycle: What to Eat",
    category: "Nutrition",
    status: "draft",
    author: "MoonSync Team",
    publishDate: null,
    views: 0,
  },
  {
    id: 4,
    title: "Managing Stress During Your Cycle",
    category: "Mental Health",
    status: "published",
    author: "Dr. Emily Chen",
    publishDate: "2024-02-05",
    views: 2103,
  },
];

const GLOBAL_CATEGORIES = ["Nutrition", "Exercise", "Mental Health", "Symptoms", "Lifestyle"];

export function Articles() {
  const [articles, setArticles] = useState(mockArticles);
  const [showEditor, setShowEditor] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [filterCategory, setFilterCategory] = useState("");

  const [formState, setFormState] = useState({ title: "", category: "Nutrition", content: "" });
  const [isPreview, setIsPreview] = useState(false);
  const textareaRef = useRef(null);
  const filteredArticles = articles.filter((article) => {
    const matchesSearch = article.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = filterStatus ? article.status === filterStatus : true;
    const matchesCategory = filterCategory ? article.category === filterCategory : true;
    return matchesSearch && matchesStatus && matchesCategory;
  });

  const filterOptions = [
    {
      label: "All Statuses",
      value: filterStatus,
      onChange: setFilterStatus,
      options: ["published", "draft"]
    },
    {
      label: "All Categories",
      value: filterCategory,
      onChange: setFilterCategory,
      options: GLOBAL_CATEGORIES
    }
  ];

  const handleFormat = (prefix, suffix = "") => {
    if (!textareaRef.current) return;
    
    const textarea = textareaRef.current;
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = formState.content.substring(start, end);
    
    // If it's a list or heading that needs to be at the start of a line, we can do smarter logic, 
    // but standard wrap is fine for most markdown editors.
    let textToInsert = selectedText;
    if (!selectedText && prefix.includes('[]')) {
       textToInsert = "link text";
    } else if (!selectedText && prefix === "![") {
       textToInsert = "alt text";
    }
    
    const newContent = 
      formState.content.substring(0, start) + 
      prefix + 
      textToInsert + 
      suffix + 
      formState.content.substring(end);
      
    setFormState(prev => ({
      ...prev,
      content: newContent
    }));
    
    // Restore cursor position after state update
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(
        start + prefix.length,
        start + prefix.length + textToInsert.length
      );
    }, 0);
  };

  const handleSave = (status) => {
    if (!formState.title.trim()) {
      toast.error("Article title is required.");
      return;
    }
    
    const newArticle = {
      id: articles.length + 1,
      title: formState.title,
      category: formState.category,
      status: status,
      author: "Admin User",
      publishDate: status === "published" ? new Date().toISOString().split('T')[0] : null,
      views: 0,
    };
    
    setArticles([newArticle, ...articles]);
    toast.success(`Article successfully ${status === 'published' ? 'published' : 'saved as draft'}.`);
    
    // reset form
    setFormState({ title: "", category: "Nutrition", content: "" });
    setShowEditor(false);
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Articles Management</h1>
          <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
            Create and manage educational content for users
          </p>
        </div>
        <ActionButton variant="primary" icon={Plus} onClick={() => setShowEditor(true)}>
          Create Article
        </ActionButton>
      </div>

      {/* Search and Filter */}
      <FilterBar
        searchPlaceholder="Search articles..."
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        filters={filterOptions}
      />

      {/* Articles Grid */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        {filteredArticles.map((article) => (
          <div
            key={article.id}
            className="flex flex-col rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] overflow-hidden transition-smooth hover:shadow-soft-lg"
          >
            {/* Article Thumbnail */}
            <div className="h-40 bg-gradient-to-br from-[var(--color-primary-light)] to-[var(--color-secondary)] flex items-center justify-center">
              <div className="text-center">
                <h4 className="text-lg font-semibold text-[var(--color-primary)] px-4">
                  {article.title}
                </h4>
              </div>
            </div>

            {/* Article Info */}
            <div className="flex flex-col flex-1 p-5">
              <div className="flex items-center justify-between mb-3">
                <span className="inline-flex items-center rounded-full bg-[var(--color-primary-light)] px-2.5 py-1 text-xs font-medium text-[var(--color-primary)]">
                  {article.category}
                </span>
                <StatusBadge status={article.status} size="sm" />
              </div>

              <p className="text-sm text-[var(--color-muted-foreground)] mb-3">
                By {article.author}
              </p>

              <div className="flex items-center gap-4 text-xs text-[var(--color-muted-foreground)] mb-4">
                {article.publishDate && (
                  <>
                    <span>Published {article.publishDate}</span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <Eye className="h-3 w-3" />
                      {article.views}
                    </span>
                  </>
                )}
                {!article.publishDate && <span>Not published yet</span>}
              </div>

              {/* Actions */}
              <div className="flex gap-2 mt-auto">
                <button className="flex-1 flex items-center justify-center gap-2 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] px-3 py-2 text-sm font-medium text-foreground hover:bg-[var(--color-muted)] transition-smooth">
                  <Edit className="h-4 w-4" />
                  Edit
                </button>
                <button className="flex items-center justify-center rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] px-3 py-2 text-sm font-medium text-[var(--color-error)] hover:bg-[var(--color-error-light)] transition-smooth">
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Article Editor Modal */}
      {showEditor && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4" onClick={() => setShowEditor(false)}>
          <div
            className="w-full max-w-3xl rounded-xl bg-[var(--color-card)] shadow-xl overflow-hidden"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Editor Header */}
            <div className="border-b border-[var(--color-border)] bg-[var(--color-secondary-bg)] px-6 py-4">
              <h3 className="text-lg font-semibold text-foreground">Create New Article</h3>
              <p className="text-sm text-[var(--color-muted-foreground)]">Write and publish educational content</p>
            </div>

            {/* Editor Content */}
            <div className="p-6 space-y-5 max-h-[70vh] overflow-y-auto">
              {/* Title */}
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Article Title
                </label>
                <input
                  type="text"
                  value={formState.title}
                  onChange={(e) => setFormState({ ...formState, title: e.target.value })}
                  placeholder="Enter article title..."
                  className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                />
              </div>

              {/* Category */}
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Category
                </label>
                <div className="relative w-full">
                  <select 
                    value={formState.category}
                    onChange={(e) => setFormState({ ...formState, category: e.target.value })}
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth appearance-none"
                  >
                    {GLOBAL_CATEGORIES.map(cat => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-4 top-1/2 -translate-y-1/2 h-5 w-5 text-[var(--color-muted-foreground)] pointer-events-none" />
                </div>
              </div>

              {/* Content */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <label className="block text-sm font-medium text-foreground">
                    Content
                  </label>
                  <div className="flex bg-[var(--color-secondary-bg)] rounded-lg p-1 border border-[var(--color-border)]">
                    <button 
                      onClick={() => setIsPreview(false)}
                      className={`px-3 py-1 text-xs font-medium rounded-md transition-colors ${!isPreview ? 'bg-white dark:bg-[var(--color-card)] text-foreground shadow-sm' : 'text-[var(--color-muted-foreground)] hover:text-foreground'}`}
                    >
                      Write
                    </button>
                    <button 
                      onClick={() => setIsPreview(true)}
                      className={`px-3 py-1 text-xs font-medium rounded-md transition-colors ${isPreview ? 'bg-white dark:bg-[var(--color-card)] text-foreground shadow-sm' : 'text-[var(--color-muted-foreground)] hover:text-foreground'}`}
                    >
                      Preview
                    </button>
                  </div>
                </div>

                <div className="rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] overflow-hidden">
                  {!isPreview && (
                    <div className="flex flex-wrap items-center gap-1 p-2 border-b border-[var(--color-border)] bg-[var(--color-secondary-bg)]">
                      <button onClick={() => handleFormat("**", "**")} title="Bold" className="rounded p-1.5 text-[var(--color-muted-foreground)] hover:text-foreground hover:bg-[var(--color-muted)] transition-smooth">
                        <strong className="font-serif">B</strong>
                      </button>
                      <button onClick={() => handleFormat("*", "*")} title="Italic" className="rounded p-1.5 text-[var(--color-muted-foreground)] hover:text-foreground italic hover:bg-[var(--color-muted)] transition-smooth font-serif">
                        I
                      </button>
                      
                      <div className="w-[1px] h-4 bg-[var(--color-border)] mx-1"></div>
                      
                      <button onClick={() => handleFormat("### ")} title="Heading" className="rounded p-1.5 text-[var(--color-muted-foreground)] hover:text-foreground font-semibold text-xs hover:bg-[var(--color-muted)] transition-smooth">
                        H
                      </button>
                      <button onClick={() => handleFormat("- ")} title="Unordered List" className="rounded p-1.5 text-[var(--color-muted-foreground)] hover:text-foreground hover:bg-[var(--color-muted)] transition-smooth text-xs">
                        • List
                      </button>
                      <button onClick={() => handleFormat("1. ")} title="Ordered List" className="rounded p-1.5 text-[var(--color-muted-foreground)] hover:text-foreground hover:bg-[var(--color-muted)] transition-smooth text-xs">
                        1. List
                      </button>

                      <div className="w-[1px] h-4 bg-[var(--color-border)] mx-1"></div>
                      
                      <button onClick={() => handleFormat("[", "](url)")} title="Link" className="rounded p-1.5 text-[var(--color-muted-foreground)] hover:text-foreground hover:bg-[var(--color-muted)] transition-smooth">
                        <LinkIcon className="h-4 w-4" />
                      </button>
                      <button onClick={() => handleFormat("![", "](image-url)")} title="Image" className="rounded p-1.5 text-[var(--color-muted-foreground)] hover:text-foreground hover:bg-[var(--color-muted)] transition-smooth">
                        <ImageIcon className="h-4 w-4" />
                      </button>
                    </div>
                  )}
                  
                  {isPreview ? (
                    <div className="p-4 min-h-[300px] bg-[var(--color-card)] text-foreground prose prose-sm dark:prose-invert max-w-none">
                      {formState.content ? (
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>
                          {formState.content}
                        </ReactMarkdown>
                      ) : (
                        <p className="text-[var(--color-muted-foreground)] italic">Nothing to preview yet...</p>
                      )}
                    </div>
                  ) : (
                    <textarea
                      ref={textareaRef}
                      value={formState.content}
                      onChange={(e) => setFormState({ ...formState, content: e.target.value })}
                      placeholder="Write your article content here... (supports markdown)"
                      className="w-full bg-[var(--color-card)] p-4 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:outline-none min-h-[300px] resize-y"
                    />
                  )}
                </div>
              </div>

              {/* Image Upload */}
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Featured Image
                </label>
                <div className="flex items-center justify-center w-full">
                  <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed rounded-lg cursor-pointer bg-[var(--color-secondary-bg)] border-[var(--color-border)] hover:bg-[var(--color-muted)] transition-smooth">
                    <div className="flex flex-col items-center justify-center pt-5 pb-6">
                      <Plus className="w-8 h-8 mb-2 text-[var(--color-muted-foreground)]" />
                      <p className="text-sm text-[var(--color-muted-foreground)]">
                        Click to upload or drag and drop
                      </p>
                    </div>
                    <input type="file" className="hidden" accept="image/*" />
                  </label>
                </div>
              </div>
            </div>

            {/* Editor Footer */}
            <div className="flex items-center justify-end gap-3 border-t border-[var(--color-border)] bg-[var(--color-secondary-bg)] px-6 py-4">
              <ActionButton variant="ghost" onClick={() => setShowEditor(false)}>
                Cancel
              </ActionButton>
              <ActionButton variant="secondary" onClick={() => handleSave("draft")}>
                Save Draft
              </ActionButton>
              <ActionButton variant="primary" onClick={() => handleSave("published")}>
                Publish Article
              </ActionButton>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

